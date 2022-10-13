package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.createFunction
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class ClassMethodsDefaultArgumentGeneratorDelegate(
    declarationBuilder: DeclarationBuilder,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    configuration: Configuration,
) : BaseDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedClasses().forEach { classDescriptor ->
            classDescriptor.allSupportedFunctions(descriptorProvider).forEach { functionDescriptor ->
                generateOverloads(functionDescriptor, classDescriptor)
            }
        }
    }

    private fun DescriptorProvider.allSupportedClasses(): List<ClassDescriptor> =
        this.classDescriptors.filter { it.isSupported }

    private val ClassDescriptor.isSupported: Boolean
        get() = when (this.kind) {
            ClassKind.CLASS, ClassKind.ENUM_CLASS, ClassKind.OBJECT -> true
            ClassKind.INTERFACE, ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> false
        }

    private fun ClassDescriptor.allSupportedFunctions(descriptorProvider: DescriptorProvider): List<SimpleFunctionDescriptor> =
        this.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.hasDefaultArguments }
            .filter { it.isSupported }
            .filter { descriptorProvider.shouldBeExposed(it) }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.dispatchReceiverParameter != null &&
                this.extensionReceiverParameter == null &&
                this.contextReceiverParameters.isEmpty()

    private fun generateOverloads(function: SimpleFunctionDescriptor, parentClass: ClassDescriptor) {
        function.forEachDefaultArgumentOverload { index, overloadParameters ->
            generateOverload(function, parentClass, index, overloadParameters)
        }
    }

    private fun generateOverload(
        function: SimpleFunctionDescriptor,
        parentClass: ClassDescriptor,
        index: Int,
        parameters: List<ValueParameterDescriptor>,
    ) {
        val newFunction = generateOverloadWithUniqueName(index, function, parentClass, parameters)

        renameOverloadedFunction(newFunction, function)
    }

    private fun generateOverloadWithUniqueName(
        index: Int,
        function: SimpleFunctionDescriptor,
        parentClass: ClassDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "__SwiftGen__${index}__${function.name.identifier}",
            namespace = declarationBuilder.getNamespace(parentClass),
            annotations = function.annotations,
        ) {
            dispatchReceiverParameter = function.dispatchReceiverParameter
            valueParameters = parameters.copyWithoutDefaultValue(descriptor)
            typeParameters = function.typeParameters
            returnType = function.returnTypeOrNothing
            isInline = function.isInline
            isSuspend = function.isSuspend
            modality = Modality.FINAL
            body = { overloadIr ->
                getOverloadBody(function, overloadIr)
            }
        }

    context(ReferenceSymbolTable, DeclarationIrBuilder) private fun getOverloadBody(
        originalFunction: FunctionDescriptor, overloadIr: IrFunction,
    ): IrBody {
        val originalFunctionSymbol = referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(originalFunctionSymbol).apply {
                    dispatchReceiver = overloadIr.dispatchReceiverParameter?.let { irGet(it) }
                    passArgumentsWithMatchingNames(overloadIr)
                }
            )
        }
    }
}

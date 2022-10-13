package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.ir.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.ir.createFunction
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class DefaultArgumentGenerator(
    private val declarationBuilder: DeclarationBuilder,
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
) : BaseGenerator(swiftFileBuilderFactory, namespaceProvider, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedFunctions()
            .filter { descriptorProvider.shouldBeExposed(it.first) }
            .filter { it.first.hasDefaultArguments }
            .forEach {
                generateOverloads(it.first, it.second)
            }
    }

    private val SimpleFunctionDescriptor.hasDefaultArguments: Boolean
        get() = this.valueParameters.any { it.declaresDefaultValue() }

    private fun DescriptorProvider.allSupportedFunctions(): List<Pair<SimpleFunctionDescriptor, ClassDescriptor>> =
        this.classDescriptors
            .filter { it.isSupported }
            .flatMap { classDescriptor ->
                classDescriptor.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
                    .filterIsInstance<SimpleFunctionDescriptor>()
                    .filter { it.canBeUsedWithExperimentalFeatures }
                    .filter { it.isSupported }
                    .map { it to classDescriptor }
            }

    private val ClassDescriptor.isSupported: Boolean
        get() = when (this.kind) {
            ClassKind.CLASS, ClassKind.ENUM_CLASS, ClassKind.OBJECT -> true
            ClassKind.INTERFACE, ClassKind.ENUM_ENTRY, ClassKind.ANNOTATION_CLASS -> false
        }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.dispatchReceiverParameter != null &&
                this.extensionReceiverParameter == null

    private fun generateOverloads(function: SimpleFunctionDescriptor, parentClass: ClassDescriptor) {
        val parametersWithDefaultValues = function.valueParameters.filter { it.hasDefaultValue() }

        parametersWithDefaultValues.forEachSubsetIndexed { index, omittedParameters ->
            if (omittedParameters.isNotEmpty()) {
                @Suppress("ConvertArgumentToSet")
                val overloadParameters = function.valueParameters - omittedParameters

                generateOverload(function, overloadParameters, parentClass, index)
            }
        }
    }

    private fun <T> Iterable<T>.forEachSubsetIndexed(action: (Int, List<T>) -> Unit) {
        var bitmap = 0
        do {
            val subset = this.dropIndices(bitmap)

            action(bitmap, subset)

            bitmap++
        } while (subset.isNotEmpty())
    }

    private fun <T> Iterable<T>.dropIndices(bitmap: Int): List<T> =
        this.filterIndexed { index, _ ->
            !bitmap.testBit(index)
        }

    private fun Int.testBit(n: Int): Boolean =
        (this shr n) and 1 == 1

    private fun generateOverload(
        function: SimpleFunctionDescriptor,
        parameters: List<ValueParameterDescriptor>,
        parentClass: ClassDescriptor,
        index: Int,
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
            valueParameters = parameters.mapIndexed { index, valueParameter -> valueParameter.copyWithoutDefaultValue(descriptor, index) }
            typeParameters = function.typeParameters
            returnType = function.returnTypeOrNothing
            isInline = function.isInline
            isSuspend = function.isSuspend
            modality = Modality.FINAL
            body = { overloadIr ->
                getOverloadBody(function, overloadIr)
            }
        }

    private fun ValueParameterDescriptor.copyWithoutDefaultValue(
        newOwner: CallableDescriptor,
        newIndex: Int,
    ): ValueParameterDescriptor = ValueParameterDescriptorImpl(
        containingDeclaration = newOwner,
        original = null,
        index = newIndex,
        annotations = Annotations.EMPTY,
        name = this.name,
        outType = this.type,
        declaresDefaultValue = false,
        isCrossinline = this.isCrossinline,
        isNoinline = this.isNoinline,
        varargElementType = this.varargElementType,
        source = SourceElement.NO_SOURCE,
    )

    context(ReferenceSymbolTable, DeclarationIrBuilder) private fun getOverloadBody(
        originalFunction: FunctionDescriptor, overloadIr: IrFunction,
    ): IrBody {
        val functionIrSymbol = referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(functionIrSymbol).apply {
                    dispatchReceiver = overloadIr.dispatchReceiverParameter?.let { irGet(it) }
                    overloadIr.valueParameters.forEach { valueParameter ->
                        val indexInCalledFunction = originalFunction.indexOfValueParameterByName(valueParameter.name)

                        putValueArgument(indexInCalledFunction, irGet(valueParameter))
                    }
                }
            )
        }
    }

    private fun FunctionDescriptor.indexOfValueParameterByName(name: Name): Int =
        this.valueParameters.indexOfFirst { it.name == name }

    private fun renameOverloadedFunction(overloadDescriptor: FunctionDescriptor, function: SimpleFunctionDescriptor) {
        val baseSignature = function.name.identifier
        val parameters = overloadDescriptor.valueParameters.joinToString("") { it.name.identifier + ":" }
        val fullSignature = "$baseSignature($parameters)"

        with(swiftPackModuleBuilder) {
            overloadDescriptor.reference().applyTransform {
                rename(fullSignature)
            }
        }
    }
}

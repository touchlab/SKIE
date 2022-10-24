package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.createFunction
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.getNamespace
import co.touchlab.swiftpack.api.SkieContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.ReferenceSymbolTable
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing

internal abstract class BaseFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : BaseDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedFunctions()
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }
            .filter { descriptorProvider.shouldBeExposed(it) }
            .forEach {
                generateOverloads(it)
            }
    }

    protected abstract fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor>

    private fun generateOverloads(function: SimpleFunctionDescriptor) {
        function.forEachDefaultArgumentOverload { index, overloadParameters ->
            generateOverload(function, index, overloadParameters)
        }
    }

    private fun generateOverload(
        function: SimpleFunctionDescriptor,
        index: Int,
        parameters: List<ValueParameterDescriptor>,
    ) {
        val newFunction = generateOverloadWithUniqueName(function, index, parameters)

        renameOverloadedFunction(newFunction, function)
    }

    private fun generateOverloadWithUniqueName(
        function: SimpleFunctionDescriptor,
        index: Int,
        parameters: List<ValueParameterDescriptor>,
    ): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "__SwiftGen__${index}__${function.name.identifier}",
            namespace = declarationBuilder.getNamespace(function),
            annotations = function.annotations,
        ) {
            dispatchReceiverParameter = function.dispatchReceiverParameter
            extensionReceiverParameter = function.extensionReceiverParameter
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

    context(ReferenceSymbolTable, DeclarationIrBuilder)
        @OptIn(ObsoleteDescriptorBasedAPI::class)
        private fun getOverloadBody(
        originalFunction: FunctionDescriptor, overloadIr: IrFunction,
    ): IrBody {
        val originalFunctionSymbol = referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(originalFunctionSymbol).apply {
                    dispatchReceiver = overloadIr.dispatchReceiverParameter?.let { irGet(it) }
                    extensionReceiver = overloadIr.extensionReceiverParameter?.let { irGet(it) }
                    passArgumentsWithMatchingNames(overloadIr)
                }
            )
        }
    }

    private fun renameOverloadedFunction(overloadDescriptor: FunctionDescriptor, function: SimpleFunctionDescriptor) {
        skieContext.module.configure {
            overloadDescriptor.swiftName.name = function.swiftName.name
        }
    }
}

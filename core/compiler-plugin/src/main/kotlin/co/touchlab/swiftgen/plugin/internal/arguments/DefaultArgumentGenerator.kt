package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.ir.IrBuilder
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
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
    private val irBuilder: IrBuilder,
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
) : BaseGenerator(swiftFileBuilderFactory, namespaceProvider, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.allSupportedFunctions()
            .filter { descriptorProvider.shouldBeExposed(it) }
            .filter { it.hasDefaultArguments }
            .forEach {
                generateOverloads(it)
            }
    }

    private val SimpleFunctionDescriptor.hasDefaultArguments: Boolean
        get() = this.valueParameters.any { it.declaresDefaultValue() }

    private fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.classDescriptors
            .flatMap { classDescriptor ->
                classDescriptor.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
                    .filterIsInstance<SimpleFunctionDescriptor>()
                    .filter { it.canBeUsedWithExperimentalFeatures }
                    .filter { it.isSupported }
            }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.dispatchReceiverParameter != null &&
                this.extensionReceiverParameter == null &&
                this.typeParameters.isEmpty()

    private fun generateOverloads(function: SimpleFunctionDescriptor) {
        val parametersWithDefaultValues = function.valueParameters.filter { it.hasDefaultValue() }

        parametersWithDefaultValues.forEachSubsetIndexed { index, omittedParameters ->
            if (omittedParameters.isNotEmpty()) {
                @Suppress("ConvertArgumentToSet")
                val overloadParameters = function.valueParameters - omittedParameters

                generateOverload(function, overloadParameters, index)
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

    private fun generateOverload(function: SimpleFunctionDescriptor, parameters: List<ValueParameterDescriptor>, index: Int) {
        irBuilder.createFunction(
            name = function.name,
            fileName = "__DefaultArguments_$index",
            annotations = function.annotations,
        ) { overload ->
            extensionReceiverParameter = function.dispatchReceiverParameter
            valueParameters = parameters.mapIndexed { index, valueParameter -> valueParameter.copyWithoutDefaultValue(overload, index) }
            returnType = function.returnTypeOrNothing
            isInline = function.isInline
            isSuspend = function.isSuspend
            body = { overloadIr ->
                getOverloadBody(function, overloadIr)
            }
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
        val functionIr = referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(functionIr).apply {
                    dispatchReceiver = overloadIr.extensionReceiverParameter?.let { irGet(it) }
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
}

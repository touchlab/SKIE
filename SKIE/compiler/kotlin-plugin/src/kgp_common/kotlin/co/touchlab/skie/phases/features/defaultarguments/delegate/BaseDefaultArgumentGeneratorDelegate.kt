package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.declaresOrInheritsDefaultValue

abstract class BaseDefaultArgumentGeneratorDelegate(
    val context: DescriptorModificationPhase.Context,
) : DefaultArgumentGeneratorDelegate {

    protected val descriptorProvider: DescriptorProvider = context.descriptorProvider

    protected val declarationBuilder: DeclarationBuilder = context.declarationBuilder

    protected val uniqueNameSubstring = "__Skie_DefaultArguments__"

    protected val FunctionDescriptor.hasDefaultArguments: Boolean
        get() = this.valueParameters.any { it.declaresOrInheritsDefaultValue() }

    private val isInteropEnabledForExternalModules: Boolean =
        SkieConfigurationFlag.Feature_DefaultArgumentsInExternalLibraries in context.skieConfiguration.enabledConfigurationFlags

    context(SkiePhase.Context)
    protected val FunctionDescriptor.isInteropEnabled: Boolean
        get() = this.getConfiguration(DefaultArgumentInterop.Enabled) &&
            this.satisfiesMaximumDefaultArgumentCount &&
            (descriptorProvider.isFromLocalModule(this) || isInteropEnabledForExternalModules)

    context(SkiePhase.Context)
    private val FunctionDescriptor.satisfiesMaximumDefaultArgumentCount: Boolean
        get() = this.defaultArgumentCount <= this.getConfiguration(DefaultArgumentInterop.MaximumDefaultArgumentCount)

    private val FunctionDescriptor.defaultArgumentCount: Int
        get() = this.valueParameters.count { it.declaresOrInheritsDefaultValue() }

    protected fun FunctionDescriptor.forEachDefaultArgumentOverload(
        action: (overloadParameters: List<ValueParameterDescriptor>) -> Unit,
    ) {
        val parametersWithDefaultValues = this.valueParameters.filter { it.declaresOrInheritsDefaultValue() }

        parametersWithDefaultValues.forEachSubset { omittedParameters ->
            if (omittedParameters.isNotEmpty()) {
                @Suppress("ConvertArgumentToSet")
                val overloadParameters = this.valueParameters - omittedParameters

                action(overloadParameters)
            }
        }
    }

    private fun <T> Iterable<T>.forEachSubset(action: (List<T>) -> Unit) {
        var bitmap = 0
        do {
            val subset = this.dropIndices(bitmap)

            action(subset)

            bitmap++
        } while (subset.isNotEmpty())
    }

    private fun <T> Iterable<T>.dropIndices(bitmap: Int): List<T> =
        this.filterIndexed { index, _ ->
            !bitmap.testBit(index)
        }

    private fun Int.testBit(n: Int): Boolean =
        (this shr n) and 1 == 1

    context(IrBuilderWithScope) protected fun IrFunctionAccessExpression.passArgumentsWithMatchingNames(from: IrFunction) {
        from.valueParameters.forEach { valueParameter: IrValueParameter ->
            val indexInCalledFunction = this.symbol.owner.indexOfValueParameterByName(valueParameter.name)
            check(indexInCalledFunction != -1) {
                "Could not find value parameter with name ${valueParameter.name} in ${this.symbol.owner} (from $from)\n\nThis dump:\n${this.dump()}\n\nFrom dump:\n${from.dump()}"
            }
            putValueArgument(indexInCalledFunction, irGet(valueParameter))
        }
    }

    protected open fun IrFunction.indexOfValueParameterByName(name: Name): Int =
        this.valueParameters.indexOfFirst { it.name == name }
}

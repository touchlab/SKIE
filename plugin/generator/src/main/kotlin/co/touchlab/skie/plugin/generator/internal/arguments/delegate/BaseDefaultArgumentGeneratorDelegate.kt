package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.DefaultArgumentInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.generator.internal.arguments.collision.CollisionDetector
import co.touchlab.skie.plugin.generator.internal.arguments.collision.toFunctionSignature
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
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

internal abstract class BaseDefaultArgumentGeneratorDelegate(
    protected val skieContext: SkieContext,
    protected val declarationBuilder: DeclarationBuilder,
    override val configuration: Configuration,
    private val collisionDetector: CollisionDetector,
    ) : DefaultArgumentGeneratorDelegate, ConfigurationContainer {

    protected val FunctionDescriptor.hasDefaultArguments: Boolean
        get() = this.valueParameters.any { it.declaresOrInheritsDefaultValue() }

    protected val FunctionDescriptor.isInteropEnabled: Boolean
        get() = this.getConfiguration(DefaultArgumentInterop.Enabled) && this.satisfiesMaximumDefaultArgumentCount

    private val FunctionDescriptor.satisfiesMaximumDefaultArgumentCount: Boolean
        get() = this.defaultArgumentCount <= this.getConfiguration(DefaultArgumentInterop.MaximumDefaultArgumentCount)

    private val FunctionDescriptor.defaultArgumentCount: Int
        get() = this.valueParameters.count { it.declaresOrInheritsDefaultValue() }

    protected fun FunctionDescriptor.forEachNonCollidingDefaultArgumentOverload(
        action: (overloadParameters: List<ValueParameterDescriptor>) -> Unit,
    ) {
        this.forEachDefaultArgumentOverload { overloadParameters ->
            val overloadSignature = this.toFunctionSignature().copy(overloadParameters)

            val createsCollision = collisionDetector.createsCollision(overloadSignature)
            if (!createsCollision) {
                action(overloadParameters)
            }
        }
    }

    private fun FunctionDescriptor.forEachDefaultArgumentOverload(
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

    private fun IrFunction.indexOfValueParameterByName(name: Name): Int =
        this.valueParameters.indexOfFirst { it.name == name }
}

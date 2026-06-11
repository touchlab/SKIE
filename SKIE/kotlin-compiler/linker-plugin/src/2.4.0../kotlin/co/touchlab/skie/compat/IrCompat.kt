@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Kotlin 2.4.0 changed the element type of `IrMutableAnnotationContainer.annotations` from `IrConstructorCall` to the
 * new `IrAnnotation` (a subtype of `IrConstructorCall`).
 */
internal typealias SkieIrAnnotation = org.jetbrains.kotlin.ir.expressions.IrAnnotation

/**
 * Kotlin 2.4.0 removed the dedicated value-parameter/receiver/argument accessors from the IR tree in favor of a single
 * positional `parameters`/`arguments` list keyed by [IrParameterKind]. These shims preserve the pre-2.4.0 semantics so
 * the shared `main` source set stays version-agnostic.
 *
 * Pre-2.4.0 `IrFunction.valueParameters` returned both [IrParameterKind.Regular] and [IrParameterKind.Context]
 * parameters (in that positional order, context first), so [isSkieValueParameter] matches that to stay faithful even
 * for functions that carry context parameters.
 */
private val IrValueParameter.isSkieValueParameter: Boolean
    get() = kind == IrParameterKind.Regular || kind == IrParameterKind.Context

internal val IrFunction.skieValueParameters: List<IrValueParameter>
    get() = parameters.filter { it.isSkieValueParameter }

internal val IrFunction.skieExtensionReceiverParameter: IrValueParameter?
    get() = parameters.firstOrNull { it.kind == IrParameterKind.ExtensionReceiver }

internal fun IrFunctionAccessExpression.skiePutValueArgument(index: Int, valueArgument: IrExpression?) {
    val valueParameters = symbol.owner.parameters.filter { it.isSkieValueParameter }

    arguments[valueParameters[index].indexInParameters] = valueArgument
}

internal fun IrFunctionAccessExpression.skiePutTypeArgument(index: Int, type: IrType?) {
    typeArguments[index] = type
}

internal var IrFunctionAccessExpression.skieExtensionReceiver: IrExpression?
    get() {
        val parameter = symbol.owner.parameters.firstOrNull { it.kind == IrParameterKind.ExtensionReceiver } ?: return null

        return arguments[parameter.indexInParameters]
    }
    set(value) {
        val parameter = symbol.owner.parameters.firstOrNull { it.kind == IrParameterKind.ExtensionReceiver } ?: return

        arguments[parameter.indexInParameters] = value
    }

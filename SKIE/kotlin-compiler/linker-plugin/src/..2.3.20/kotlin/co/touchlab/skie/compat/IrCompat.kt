@file:Suppress("invisible_reference", "invisible_member", "DEPRECATION")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType

internal typealias SkieIrAnnotation = org.jetbrains.kotlin.ir.expressions.IrConstructorCall

internal val IrFunction.skieValueParameters: List<IrValueParameter>
    get() = valueParameters

internal val IrFunction.skieExtensionReceiverParameter: IrValueParameter?
    get() = extensionReceiverParameter

internal fun IrFunctionAccessExpression.skiePutValueArgument(index: Int, valueArgument: IrExpression?) {
    putValueArgument(index, valueArgument)
}

internal fun IrFunctionAccessExpression.skiePutTypeArgument(index: Int, type: IrType?) {
    putTypeArgument(index, type)
}

internal var IrFunctionAccessExpression.skieExtensionReceiver: IrExpression?
    get() = extensionReceiver
    set(value) {
        extensionReceiver = value
    }

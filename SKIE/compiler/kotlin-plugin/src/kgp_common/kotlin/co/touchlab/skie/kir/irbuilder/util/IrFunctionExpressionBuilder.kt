package co.touchlab.skie.kir.irbuilder.util

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.IrType

fun IrBuilderWithScope.irFunctionExpression(
    type: IrType,
    origin: IrStatementOrigin,
    function: IrSimpleFunction,
): IrFunctionExpression = IrFunctionExpressionImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
    function = function,
    origin = origin,
)

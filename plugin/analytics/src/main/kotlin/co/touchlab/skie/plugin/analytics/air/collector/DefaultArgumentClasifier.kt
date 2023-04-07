package co.touchlab.skie.plugin.analytics.air.collector

import co.touchlab.skie.plugin.api.air.element.AirValueParameter
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstantValue
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

object AirDefaultArgumentClassifier {

    fun classifyDefaultArgument(irExpressionBody: IrExpressionBody): AirValueParameter.DefaultValueKind =
        when {
            irExpressionBody.expression.isConstant -> AirValueParameter.DefaultValueKind.Constant
            !irExpressionBody.expression.containsValueParameterAccess -> AirValueParameter.DefaultValueKind.GlobalExpression
            else -> AirValueParameter.DefaultValueKind.LocalExpression
        }

    private val IrExpression.isConstant: Boolean
        get() = this is IrConst<*> || this is IrConstantValue

    private val IrExpression.containsValueParameterAccess: Boolean
        get() = try {
            this.acceptVoid(ValueParameterAccessChecker)

            false
        } catch (_: ContainsValueParameterAccess) {
            true
        }

    private object ValueParameterAccessChecker: IrElementVisitorVoid {

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitGetValue(expression: IrGetValue) {
            super.visitGetValue(expression)

            // This is a slight simplification - in theory there can be a IrValueParameter that does not belong to the receiver nor the function.
            // However, this situation might happen only if the default argument is a complex expression with an inner function.
            if (expression.symbol.owner is IrValueParameter) {
                throw ContainsValueParameterAccess()
            }
        }
    }

    private class ContainsValueParameterAccess : RuntimeException()
}

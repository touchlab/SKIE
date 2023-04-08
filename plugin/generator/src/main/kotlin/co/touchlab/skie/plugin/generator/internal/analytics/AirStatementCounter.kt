package co.touchlab.skie.plugin.generator.internal.analytics

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

object AirStatementCounter {

    fun statementSize(body: IrBody?): Int =
        when (body) {
            is IrBlockBody, is IrExpressionBody -> body.statements.sumOf { statementSize(it) }
            else -> 0
        }

    fun statementSize(statement: IrStatement?): Int {
        val counter = Counter()

        statement?.acceptVoid(counter)

        return counter.count
    }

    private class Counter : IrElementVisitorVoid {

        var count = 0

        override fun visitElement(element: IrElement) {
            count++

            element.acceptChildrenVoid(this)
        }
    }
}

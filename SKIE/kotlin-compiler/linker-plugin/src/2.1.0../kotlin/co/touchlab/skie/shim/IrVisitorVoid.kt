package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

abstract class IrVisitorVoid : org.jetbrains.kotlin.ir.visitors.IrVisitorVoid()

fun IrVisitorVoid.visitChildren(element: IrElement) {
    element.acceptChildrenVoid(this)
}

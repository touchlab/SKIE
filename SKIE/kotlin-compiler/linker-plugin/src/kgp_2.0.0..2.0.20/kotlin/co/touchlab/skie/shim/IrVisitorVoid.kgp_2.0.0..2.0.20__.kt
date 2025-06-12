package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

actual abstract class IrVisitorVoid: IrElementVisitorVoid {

    actual override fun visitModuleFragment(declaration: IrModuleFragment) {
        super.visitModuleFragment(declaration)
    }
}

actual fun IrVisitorVoid.visitChildren(element: IrElement) {
    element.acceptChildrenVoid(this)
}

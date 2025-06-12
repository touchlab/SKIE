package co.touchlab.skie.shim

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty

expect abstract class IrVisitorVoid {

    constructor()

    open fun visitElement(element: IrElement)

    open fun visitClass(declaration: IrClass)

    open fun visitFunction(declaration: IrFunction)

    open fun visitProperty(declaration: IrProperty)

    open fun visitModuleFragment(declaration: IrModuleFragment)
}

expect fun IrVisitorVoid.visitChildren(element: IrElement)

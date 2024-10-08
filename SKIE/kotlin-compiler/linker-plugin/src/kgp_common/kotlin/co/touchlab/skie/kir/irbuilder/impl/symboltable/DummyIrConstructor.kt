package co.touchlab.skie.kir.irbuilder.impl.symboltable

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol

expect class DummyIrConstructor(symbol: IrConstructorSymbol) : IrConstructor {
    override var isPrimary: Boolean
    override var isExpect: Boolean
    override var isInline: Boolean
    override var isExternal: Boolean
}

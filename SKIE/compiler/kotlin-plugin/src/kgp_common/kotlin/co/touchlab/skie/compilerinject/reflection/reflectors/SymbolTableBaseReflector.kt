package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

class SymbolTableBaseReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.ir.util.SymbolTable\$SymbolTableBase") {

    val unboundSymbols by declaredProperty<MutableSet<IrSimpleFunctionSymbol>>()
}

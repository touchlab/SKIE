package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.Reflector
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

internal class SymbolTableBaseReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.ir.util.SymbolTable\$SymbolTableBase") {

    val unboundSymbols by declaredProperty<MutableSet<IrSimpleFunctionSymbol>>()
}

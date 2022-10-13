package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.ir.symbols.IrSymbol

internal class SymbolTableBaseReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.ir.util.SymbolTable\$SymbolTableBase") {

    val unboundSymbols by declaredProperty<MutableSet<IrSymbol>>()
}

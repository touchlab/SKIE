package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class SymbolTableReflector(
    override val instance: SymbolTable,
) : Reflector(SymbolTable::class) {

    val simpleFunctionSymbolTable by declaredField<Any>()
}

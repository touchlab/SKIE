package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.ir.util.SymbolTable

class SymbolTableReflector(
    override val instance: SymbolTable,
) : Reflector(SymbolTable::class) {

    val simpleFunctionSymbolTable by declaredField<Any>()
}

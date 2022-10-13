package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class SymbolTableReflector(
    override val instance: SymbolTable,
) : Reflector(SymbolTable::class) {

    val simpleFunctionSymbolTable by declaredField<Any>()

    val constructorSymbolTable by declaredField<Any>()
}

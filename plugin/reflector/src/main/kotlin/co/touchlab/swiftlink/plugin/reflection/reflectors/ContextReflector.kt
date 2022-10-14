package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.Reflector
import org.jetbrains.kotlin.ir.util.SymbolTable

internal class ContextReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.backend.konan.Context") {

    val symbolTable by extensionProperty<SymbolTable>("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
}

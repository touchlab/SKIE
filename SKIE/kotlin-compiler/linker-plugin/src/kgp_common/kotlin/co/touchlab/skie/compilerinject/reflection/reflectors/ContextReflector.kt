package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.ir.util.SymbolTable

class ContextReflector(override val instance: Any) : Reflector("org.jetbrains.kotlin.backend.konan.Context") {

    val symbolTable by extensionProperty<SymbolTable>("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
}

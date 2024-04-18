package co.touchlab.skie.phases

import co.touchlab.skie.context.SymbolTablePhaseContext
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable

val SymbolTablePhase.Context.declarationBuilder: DeclarationBuilderImpl
    get() = typedContext.declarationBuilder

val SymbolTablePhase.Context.skieSymbolTable: SkieSymbolTable
    get() = typedContext.skieSymbolTable

private val SymbolTablePhase.Context.typedContext: SymbolTablePhaseContext
    get() = context as SymbolTablePhaseContext

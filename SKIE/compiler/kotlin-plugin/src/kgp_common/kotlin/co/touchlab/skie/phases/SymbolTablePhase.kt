package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable

interface SymbolTablePhase : ForegroundCompilerPhase<SymbolTablePhase.Context> {

    interface Context : ForegroundCompilerPhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilderImpl

        val skieSymbolTable: SkieSymbolTable
    }
}

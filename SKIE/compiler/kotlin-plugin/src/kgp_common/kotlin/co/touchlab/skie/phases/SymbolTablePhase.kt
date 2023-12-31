package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.util.SkieSymbolTable

interface SymbolTablePhase : SkiePhase<SymbolTablePhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilderImpl

        val skieSymbolTable: SkieSymbolTable
    }
}

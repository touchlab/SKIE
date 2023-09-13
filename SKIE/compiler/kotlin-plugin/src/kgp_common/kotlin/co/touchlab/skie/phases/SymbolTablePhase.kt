package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.ir.util.SymbolTable

interface SymbolTablePhase : SkiePhase<SymbolTablePhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilderImpl

        val symbolTable: SymbolTable
    }
}

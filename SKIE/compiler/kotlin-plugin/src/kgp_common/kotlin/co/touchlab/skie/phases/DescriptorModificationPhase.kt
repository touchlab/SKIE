package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface DescriptorModificationPhase : ForegroundCompilerPhase<DescriptorModificationPhase.Context> {

    interface Context : ForegroundCompilerPhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilder
    }
}

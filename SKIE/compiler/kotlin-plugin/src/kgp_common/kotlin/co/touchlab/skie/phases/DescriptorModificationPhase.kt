package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface DescriptorModificationPhase : CompilerDependentForegroundPhase<DescriptorModificationPhase.Context> {

    interface Context : CompilerDependentForegroundPhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilder
    }
}

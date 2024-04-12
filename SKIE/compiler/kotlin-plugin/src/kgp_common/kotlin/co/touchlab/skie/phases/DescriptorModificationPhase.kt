package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface DescriptorModificationPhase : ForegroundPhase<DescriptorModificationPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val declarationBuilder: DeclarationBuilder
    }
}

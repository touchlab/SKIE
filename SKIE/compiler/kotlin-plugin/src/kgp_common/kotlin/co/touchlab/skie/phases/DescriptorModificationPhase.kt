package co.touchlab.skie.phases

import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface DescriptorModificationPhase : SkiePhase<DescriptorModificationPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context
            get() = this

        val declarationBuilder: DeclarationBuilder
    }
}

package co.touchlab.skie.phases

import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl

interface ClassExportPhase : SkiePhase<ClassExportPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context
            get() = this

        override val descriptorProvider: MutableDescriptorProvider

        val declarationBuilder: DeclarationBuilder
    }
}

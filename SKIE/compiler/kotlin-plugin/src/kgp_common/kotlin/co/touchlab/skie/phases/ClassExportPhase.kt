package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface ClassExportPhase : ForegroundPhase<ClassExportPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        override val descriptorProvider: MutableDescriptorProvider

        val declarationBuilder: DeclarationBuilder
    }
}

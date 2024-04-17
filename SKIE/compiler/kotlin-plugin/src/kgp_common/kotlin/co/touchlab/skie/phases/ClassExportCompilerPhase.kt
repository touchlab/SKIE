package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface ClassExportCompilerPhase :
    ClassExportPhase<ClassExportCompilerPhase.Context>,
    ForegroundCompilerPhase<ClassExportCompilerPhase.Context> {

    interface Context : ClassExportPhase.Context, ForegroundCompilerPhase.Context {

        override val context: Context

        override val descriptorProvider: MutableDescriptorProvider

        val declarationBuilder: DeclarationBuilder
    }
}

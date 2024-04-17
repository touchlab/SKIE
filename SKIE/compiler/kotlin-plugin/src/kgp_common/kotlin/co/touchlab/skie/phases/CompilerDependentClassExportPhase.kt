package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder

interface CompilerDependentClassExportPhase :
    ClassExportPhase<CompilerDependentClassExportPhase.Context>,
    CompilerDependentForegroundPhase<CompilerDependentClassExportPhase.Context> {

    interface Context : ClassExportPhase.Context, CompilerDependentForegroundPhase.Context {

        override val context: Context

        override val descriptorProvider: MutableDescriptorProvider

        val declarationBuilder: DeclarationBuilder
    }
}

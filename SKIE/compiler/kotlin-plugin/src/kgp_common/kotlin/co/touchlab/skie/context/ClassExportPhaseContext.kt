package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.ClassExportCompilerPhase
import co.touchlab.skie.phases.ForegroundCompilerPhase

class ClassExportPhaseContext(
    mainSkieContext: MainSkieContext,
) : ClassExportCompilerPhase.Context, ForegroundCompilerPhase.Context by mainSkieContext {

    override val context: ClassExportPhaseContext = this

    override val descriptorProvider: MutableDescriptorProvider = mainSkieContext.descriptorProvider

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}

package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.CompilerDependentForegroundPhase
import co.touchlab.skie.phases.ForegroundPhase

class ClassExportPhaseContext(
    mainSkieContext: MainSkieContext,
) : ClassExportPhase.Context, CompilerDependentForegroundPhase.Context by mainSkieContext {

    override val context: ClassExportPhaseContext = this

    override val descriptorProvider: MutableDescriptorProvider = mainSkieContext.descriptorProvider

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}

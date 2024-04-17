package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.CompilerDependentClassExportPhase
import co.touchlab.skie.phases.CompilerDependentForegroundPhase

class ClassExportPhaseContext(
    mainSkieContext: MainSkieContext,
) : CompilerDependentClassExportPhase.Context, CompilerDependentForegroundPhase.Context by mainSkieContext {

    override val context: ClassExportPhaseContext = this

    override val descriptorProvider: MutableDescriptorProvider = mainSkieContext.descriptorProvider

    override val declarationBuilder: DeclarationBuilderImpl = mainSkieContext.declarationBuilder
}

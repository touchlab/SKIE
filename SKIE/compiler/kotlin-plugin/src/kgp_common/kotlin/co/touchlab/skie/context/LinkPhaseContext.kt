package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.phases.CompilerDependentForegroundPhase
import co.touchlab.skie.phases.CompilerDependentLinkPhase
import java.nio.file.Path

class LinkPhaseContext(
    private val mainSkieContext: MainSkieContext,
    private val link: (additionalObjectFiles: List<Path>) -> Unit,
) : CompilerDependentLinkPhase.Context, CompilerDependentForegroundPhase.Context by mainSkieContext {

    override val context: CompilerDependentLinkPhase.Context = this

    override val descriptorKirProvider: DescriptorKirProvider = mainSkieContext.descriptorKirProvider

    override fun link(additionalObjectFiles: List<Path>) {
        link.invoke(additionalObjectFiles)
    }

    override suspend fun awaitAllBackgroundJobs() {
        mainSkieContext.awaitAllBackgroundJobs()
    }
}

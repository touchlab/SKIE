package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.phases.ForegroundPhase
import co.touchlab.skie.phases.LinkPhase
import java.nio.file.Path

class LinkPhaseContext(
    private val mainSkieContext: MainSkieContext,
    private val link: (additionalObjectFiles: List<Path>) -> Unit,
) : LinkPhase.Context, ForegroundPhase.Context by mainSkieContext {

    override val context: LinkPhase.Context = this

    override val descriptorKirProvider: DescriptorKirProvider = mainSkieContext.descriptorKirProvider

    override fun link(additionalObjectFiles: List<Path>) {
        link.invoke(additionalObjectFiles)
    }

    override suspend fun awaitAllBackgroundJobs() {
        mainSkieContext.awaitAllBackgroundJobs()
    }
}

package co.touchlab.skie.context

import co.touchlab.skie.phases.LinkPhase
import co.touchlab.skie.phases.SkiePhase
import java.nio.file.Path

class LinkPhaseContext(
    private val mainSkieContext: MainSkieContext,
    private val link: (additionalObjectFiles: List<Path>) -> Unit,
) : LinkPhase.Context, SkiePhase.Context by mainSkieContext {

    override val context: LinkPhase.Context = this

    override fun link(additionalObjectFiles: List<Path>) {
        link.invoke(additionalObjectFiles)
    }

    override suspend fun awaitAllBackgroundJobs() {
        mainSkieContext.awaitAllBackgroundJobs()
    }
}

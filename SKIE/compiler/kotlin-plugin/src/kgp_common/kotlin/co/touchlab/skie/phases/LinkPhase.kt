package co.touchlab.skie.phases

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import java.nio.file.Path

interface LinkPhase : ForegroundPhase<LinkPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        val descriptorKirProvider: DescriptorKirProvider

        fun link(additionalObjectFiles: List<Path>)

        suspend fun awaitAllBackgroundJobs()
    }
}

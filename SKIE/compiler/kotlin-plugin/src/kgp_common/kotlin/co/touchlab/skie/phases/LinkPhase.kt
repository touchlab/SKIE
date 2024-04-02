package co.touchlab.skie.phases

import java.nio.file.Path

interface LinkPhase : SkiePhase<LinkPhase.Context> {

    interface Context : SkiePhase.Context {

        override val context: Context

        fun link(additionalObjectFiles: List<Path>)

        suspend fun awaitAllBackgroundJobs()
    }
}

package co.touchlab.skie.phases

import java.nio.file.Path

interface LinkPhase : ForegroundPhase<LinkPhase.Context> {

    interface Context : ForegroundPhase.Context {

        override val context: Context

        fun link(additionalObjectFiles: List<Path>)

        suspend fun awaitAllBackgroundJobs()
    }
}

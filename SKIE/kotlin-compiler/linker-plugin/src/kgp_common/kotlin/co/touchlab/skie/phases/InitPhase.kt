package co.touchlab.skie.phases

import co.touchlab.skie.context.CommonSkieContext
import org.jetbrains.kotlin.config.CompilerConfiguration

object InitPhase {

    interface Context : CommonSkieContext {

        override val context: Context

        val compilerConfiguration: CompilerConfiguration

        val skiePhaseScheduler: SkiePhaseScheduler
    }
}

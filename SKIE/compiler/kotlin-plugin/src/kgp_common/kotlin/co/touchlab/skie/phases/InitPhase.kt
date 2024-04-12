package co.touchlab.skie.phases

import org.jetbrains.kotlin.config.CompilerConfiguration

object InitPhase {

    interface Context : CommonSkieContext {

        override val context: Context

        val compilerConfiguration: CompilerConfiguration
    }
}

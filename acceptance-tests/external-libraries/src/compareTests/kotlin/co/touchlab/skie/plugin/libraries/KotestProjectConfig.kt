package co.touchlab.skie.plugin.libraries

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel

// Loaded by Kotest in runtime
@Suppress("unused")
object KotestProjectConfig: AbstractProjectConfig() {
    override val logLevel: LogLevel? = LogLevel.Info
}

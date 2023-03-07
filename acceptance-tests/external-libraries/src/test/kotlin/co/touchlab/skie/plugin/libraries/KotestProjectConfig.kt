package co.touchlab.skie.plugin.libraries

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.config.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

// Loaded by Kotest in runtime
@Suppress("unused")
object KotestProjectConfig: AbstractProjectConfig() {
    override val timeout: Duration = 10.hours
    override val invocationTimeout: Long = 10.hours.inWholeMilliseconds
    override val projectTimeout: Duration = 20.hours
}

package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

// Loaded by Kotest in runtime
@Suppress("unused")
object ProjectConfig : AbstractProjectConfig() {

    override val timeout: Duration = 1.hours
    override val invocationTimeout: Duration = 1.hours
    override val projectTimeout: Duration = 2.hours
}

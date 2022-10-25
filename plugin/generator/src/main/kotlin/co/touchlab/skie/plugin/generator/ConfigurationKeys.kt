package co.touchlab.skie.plugin.generator

import co.touchlab.skie.configuration.Configuration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigurationKeys {
    val swiftGenConfiguration = CompilerConfigurationKey<Configuration>("SwiftGen configuration")
}

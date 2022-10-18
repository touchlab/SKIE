package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.configuration.Configuration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigurationKeys {
    val swiftGenConfiguration = CompilerConfigurationKey<Configuration>("SwiftGen configuration")
}

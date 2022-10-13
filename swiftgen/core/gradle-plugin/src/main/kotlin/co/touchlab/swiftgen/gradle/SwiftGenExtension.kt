package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.builder.ConfigurationBuilder

open class SwiftGenExtension {

    private val configurationBuilder = ConfigurationBuilder()

    fun configuration(builder: ConfigurationBuilder.() -> Unit) {
        configurationBuilder.apply(builder)
    }

    internal fun buildConfiguration(): Configuration =
        Configuration(configurationBuilder)
}
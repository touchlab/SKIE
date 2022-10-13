package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.plugin.internal.util.ir.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.swiftgen.configuration.Configuration as ConfigurationType

internal sealed class SwiftGenCompilerConfigurationKey<T : Any>(name: String) {

    private val key: CompilerConfigurationKey<T> = CompilerConfigurationKey(name)

    fun exists(configuration: CompilerConfiguration): Boolean =
        configuration.get(key) != null

    fun put(value: T, configuration: CompilerConfiguration) {
        configuration.put(key, value)
    }

    fun get(configuration: CompilerConfiguration): T =
        configuration.get(key) ?: throw IllegalArgumentException("Missing configuration for key $key.")

    fun getOrNull(configuration: CompilerConfiguration): T? =
        configuration.get(key)

    object Configuration : SwiftGenCompilerConfigurationKey<ConfigurationType>("Configuration")

    object DeclarationBuilder : SwiftGenCompilerConfigurationKey<DeclarationBuilderImpl>("DeclarationBuilder")
}

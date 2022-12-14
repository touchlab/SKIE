package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.generator.internal.util.DescriptorProvider as DescriptorProviderInstance
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.skie.configuration.Configuration as ConfigurationType

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

    object DescriptorProvider : SwiftGenCompilerConfigurationKey<DescriptorProviderInstance>("Descriptor Provider")
}

internal val CommonBackendContext.skieDeclarationBuilder: DeclarationBuilder
    get() = SwiftGenCompilerConfigurationKey.DeclarationBuilder.get(configuration)

internal val CommonBackendContext.skieDescriptorProvider: DescriptorProviderInstance
    get() = SwiftGenCompilerConfigurationKey.DescriptorProvider.get(configuration)

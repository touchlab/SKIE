package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.skie.configuration.Configuration as ConfigurationType
import co.touchlab.skie.plugin.generator.internal.SkieCompilationScheduler as SkieSchedulerType

internal sealed class SkieCompilerConfigurationKey<T : Any>(name: String) {

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

    object Configuration : SkieCompilerConfigurationKey<ConfigurationType>("Configuration")

    object DeclarationBuilder : SkieCompilerConfigurationKey<DeclarationBuilderImpl>("Skie Declaration builder")

    object SkieScheduler : SkieCompilerConfigurationKey<SkieSchedulerType>("Skie Scheduler")
}

internal val KonanConfig.skieDeclarationBuilder: DeclarationBuilderImpl
    get() = SkieCompilerConfigurationKey.DeclarationBuilder.get(configuration)

internal val KonanConfig.skieScheduler: SkieSchedulerType
    get() = SkieCompilerConfigurationKey.SkieScheduler.get(configuration)

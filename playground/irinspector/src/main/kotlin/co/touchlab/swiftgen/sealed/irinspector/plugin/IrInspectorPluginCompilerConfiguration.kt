package co.touchlab.swiftgen.sealed.irinspector.plugin

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

sealed interface IrInspectorPluginCompilerConfiguration<T: Any> {

    val key: CompilerConfigurationKey<T>

    fun put(value: T, configuration: CompilerConfiguration) {
        configuration.put(key, value)
    }

    fun get(configuration: CompilerConfiguration): T =
        configuration.get(key) ?: throw IllegalArgumentException("Missing configuration for $key.")

    object OutputPath: IrInspectorPluginCompilerConfiguration<String> {

        override val key: CompilerConfigurationKey<String> = CompilerConfigurationKey(IrInspectorPluginOption.Output.optionName)
    };
}

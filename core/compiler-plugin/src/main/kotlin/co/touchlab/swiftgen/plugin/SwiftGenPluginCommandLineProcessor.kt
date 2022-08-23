package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.SwiftGenCompilerConfiguration
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftGenPluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = BuildConfig.PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> =
        SwiftGenConfiguration().options.map {
            CliOption(
                optionName = it.name,
                valueDescription = it.valueDescription,
                description = it.description,
            )
        }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        if (!SwiftGenCompilerConfiguration.Key.exists(configuration)) {
            SwiftGenCompilerConfiguration.Key.put(SwiftGenConfiguration(), configuration)
        }

        SwiftGenCompilerConfiguration.Key.get(configuration).set(option.optionName, value)
    }
}

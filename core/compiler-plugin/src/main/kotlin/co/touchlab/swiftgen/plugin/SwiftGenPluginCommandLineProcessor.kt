package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.SwiftGenCompilerConfiguration
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.nio.file.Path
import kotlin.io.path.readText

class SwiftGenPluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = BuildConfig.PLUGIN_ID

    private val configPathOption = CliOption(
        optionName = Configuration.CliOptionKey,
        valueDescription = "Path",
        description = "Path to JSON file with SwiftGen configuration.",
    )

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(configPathOption)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        if (option.optionName == configPathOption.optionName) {
            val configPath = Path.of(value)

            val configText = configPath.readText()

            val swiftGenConfiguration = Configuration.deserialize(configText)

            SwiftGenCompilerConfiguration.Key.put(swiftGenConfiguration, configuration)
        }
    }
}

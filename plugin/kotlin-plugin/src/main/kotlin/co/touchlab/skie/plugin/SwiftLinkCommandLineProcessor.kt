package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.SkiePlugin.Options
import co.touchlab.skie.plugin.util.toCliOption
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftLinkCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = SkiePlugin.id

    private val options = listOf(
        Options.buildId,
        Options.skieDirectories,
    )

    private val optionsMap = options.associateBy { it.optionName }

    override val pluginOptions: Collection<AbstractCliOption> = options.map { it.toCliOption() }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (optionsMap[option.optionName]) {
            Options.buildId -> {
                configuration.put(ConfigurationKeys.buildId, Options.buildId.deserialize(value))
            }
            Options.skieDirectories -> {
                configuration.put(ConfigurationKeys.skieDirectories, Options.skieDirectories.deserialize(value))
            }
        }
    }
}

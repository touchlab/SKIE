package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.util.toCliOption
import co.touchlab.skie.util.plugin.SkiePlugin
import co.touchlab.skie.util.plugin.SkiePlugin.Options
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
class SwiftLinkCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = SkiePlugin.id

    private val options = listOf(
        Options.skieDirectories,
    )

    private val optionsMap = options.associateBy { it.optionName }

    override val pluginOptions: Collection<AbstractCliOption> = options.map { it.toCliOption() }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (optionsMap[option.optionName]) {
            Options.skieDirectories -> {
                configuration.put(ConfigurationKeys.skieDirectories, Options.skieDirectories.deserialize(value))
            }
        }
    }
}

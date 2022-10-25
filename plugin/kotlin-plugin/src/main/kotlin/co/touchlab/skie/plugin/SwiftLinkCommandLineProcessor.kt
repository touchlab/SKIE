package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.SkiePlugin.Options
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import co.touchlab.skie.plugin.generator.ConfigurationKeys as SwiftGenConfigurationKeys

class SwiftLinkCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = SkiePlugin.id

    private val options = listOf(
        Options.swiftSourceFile,
        Options.generatedSwiftDir,
        Options.disableWildcardExport,
        Options.swiftGenConfigPath,
    )
    private val optionsMap = options.associateBy { it.optionName }
    override val pluginOptions: Collection<AbstractCliOption> = options.map { it.toCliOption() }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (optionsMap[option.optionName]) {
            Options.swiftSourceFile -> {
                configuration.add(ConfigurationKeys.swiftSourceFiles, Options.swiftSourceFile.deserialize(value))
            }

            Options.generatedSwiftDir -> {
                configuration.putIfNotNull(ConfigurationKeys.generatedSwiftDir, Options.generatedSwiftDir.deserialize(value))
            }

            Options.disableWildcardExport -> {
                configuration.putIfNotNull(ConfigurationKeys.disableWildcardExport, Options.disableWildcardExport.deserialize(value))
            }

            Options.swiftGenConfigPath -> {
                val config = Options.swiftGenConfigPath.deserialize(value).readText()
                val swiftGenConfiguration = Configuration.deserialize(config)

                configuration.put(SwiftGenConfigurationKeys.swiftGenConfiguration, swiftGenConfiguration)
            }
        }
    }
}

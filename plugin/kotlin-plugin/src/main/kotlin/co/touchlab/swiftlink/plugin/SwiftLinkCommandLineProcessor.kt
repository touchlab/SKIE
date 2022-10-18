package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.configuration.Configuration
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import co.touchlab.swiftlink.plugin.SkiePlugin.Options

class SwiftLinkCommandLineProcessor: CommandLineProcessor {

    override val pluginId: String = SkiePlugin.id

    private val options = listOf(
        Options.swiftPackModule,
        Options.swiftSourceFile,
        Options.expandedSwiftDir,
        Options.linkPhaseSwiftPackOutputDir,
        Options.disableWildcardExport,
        Options.swiftGenConfigPath,
    )
    private val optionsMap = options.associateBy { it.optionName }
    override val pluginOptions: Collection<AbstractCliOption> = options.map { it.toCliOption() }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (optionsMap[option.optionName]) {
            Options.swiftPackModule -> {
                configuration.add(ConfigurationKeys.swiftPackModules, Options.swiftPackModule.deserialize(value))
            }
            Options.swiftSourceFile -> {
                configuration.add(ConfigurationKeys.swiftSourceFiles, Options.swiftSourceFile.deserialize(value))
            }
            Options.expandedSwiftDir -> {
                configuration.putIfNotNull(ConfigurationKeys.expandedSwiftDir, Options.expandedSwiftDir.deserialize(value))
            }
            Options.linkPhaseSwiftPackOutputDir -> {
                configuration.putIfNotNull(ConfigurationKeys.linkPhaseSwiftPackOutputDir, Options.linkPhaseSwiftPackOutputDir.deserialize(value))
            }
            Options.disableWildcardExport -> {
                configuration.putIfNotNull(ConfigurationKeys.disableWildcardExport, Options.disableWildcardExport.deserialize(value))
            }
            Options.swiftGenConfigPath -> {
                val config = Options.swiftGenConfigPath.deserialize(value).readText()
                val swiftGenConfiguration = Configuration.deserialize(config)

                configuration.put(ConfigurationKeys.swiftGenConfiguration, swiftGenConfiguration)
            }
        }
    }
}

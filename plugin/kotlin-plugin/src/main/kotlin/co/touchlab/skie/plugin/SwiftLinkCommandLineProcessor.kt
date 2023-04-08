package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.SkiePlugin.Options
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.util.toCliOption
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

class SwiftLinkCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = SkiePlugin.id

    private val options = listOf(
        Options.swiftSourceFile,
        Options.generatedSwiftDir,
        Options.disableWildcardExport,
        Options.skieConfigurationPath,
        Options.buildId,
        Options.analyticsDir,
        Options.Debug.infoDirectory,
        Options.Debug.dumpSwiftApiAt,
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

            Options.skieConfigurationPath -> {
                val config = Options.skieConfigurationPath.deserialize(value).readText()
                val skieConfiguration = Configuration.deserialize(config)

                configuration.put(ConfigurationKeys.skieConfiguration, skieConfiguration)
            }

            Options.buildId -> {
                configuration.put(ConfigurationKeys.buildId, Options.buildId.deserialize(value))
            }

            Options.analyticsDir -> {
                configuration.put(ConfigurationKeys.analyticsDir, Options.analyticsDir.deserialize(value))
            }

            Options.Debug.infoDirectory -> {
                configuration.putIfNotNull(
                    ConfigurationKeys.Debug.infoDirectory,
                    DebugInfoDirectory(Options.Debug.infoDirectory.deserialize(value)),
                )
            }

            Options.Debug.dumpSwiftApiAt -> {
                configuration.addToSet(ConfigurationKeys.Debug.dumpSwiftApiPoints, Options.Debug.dumpSwiftApiAt.deserialize(value))
            }
        }
    }

    private fun <T> CompilerConfiguration.addToSet(option: CompilerConfigurationKey<Set<T>>, values: T) {
        val paths = get(option) ?: emptySet()
        put(option, paths + values)
    }
}

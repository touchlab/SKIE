package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

@AutoService(CommandLineProcessor::class)
class SwiftKtCommandLineProcessor: CommandLineProcessor {
    companion object {
        const val pluginId = "co.touchlab.swiftkt"
    }

    override val pluginId: String = SwiftKtCommandLineProcessor.pluginId

    private val options = listOf(
        Options.swiftPackModule,
        Options.expandedSwiftDir,
    )
    private val optionsMap = options.map { it.optionName to it }.toMap()
    override val pluginOptions: Collection<AbstractCliOption> = options.map { it.toCliOption() }

    object Options {
        val swiftPackModule = PluginOption(
            optionName = "swiftPackModule",
            valueDescription = "<namespace>${File.pathSeparator}<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            serialize = { (namespace, moduleFile) ->
                "$namespace${File.pathSeparator}${moduleFile.absolutePath}"
            },
            deserialize = { value ->
                val (namespace, absolutePath) = value.split(File.pathSeparator)
                NamespacedSwiftPackModule.Reference(namespace, File(absolutePath))
            },
        )

        val expandedSwiftDir = PluginOption(
            optionName = "expandedSwiftDir",
            valueDescription = "<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            isRequired = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )
    }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (optionsMap[option.optionName]) {
            Options.swiftPackModule -> {
                val (namespace, moduleFile) = Options.swiftPackModule.deserialize(value)
                val namespacedModule = NamespacedSwiftPackModule(namespace, SwiftPackModule.read(moduleFile))
                configuration.add(ConfigurationKeys.swiftPackModules, namespacedModule)
            }
            Options.expandedSwiftDir -> {
                configuration.putIfNotNull(ConfigurationKeys.expandedSwiftDir, Options.expandedSwiftDir.deserialize(value))
            }
        }
    }
}

object ConfigurationKeys {
    val swiftPackModules = CompilerConfigurationKey<List<NamespacedSwiftPackModule>>("SwiftPack modules")
    val expandedSwiftDir = CompilerConfigurationKey<File>("expanded Swift directory")
}

data class PluginOption<T>(
    val optionName: String,
    val valueDescription: String,
    val description: String,
    val isRequired: Boolean = false,
    val allowMultipleOccurrences: Boolean = false,
    val serialize: (T) -> String,
    val deserialize: (String) -> T
) {
    fun toCliOption() = CliOption(
        optionName,
        valueDescription,
        description,
        isRequired,
        allowMultipleOccurrences
    )
}

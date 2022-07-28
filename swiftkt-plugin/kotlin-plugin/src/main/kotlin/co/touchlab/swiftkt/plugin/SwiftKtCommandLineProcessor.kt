package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File

@AutoService(CommandLineProcessor::class)
class SwiftKtCommandLineProcessor: CommandLineProcessor {
    companion object {
        const val pluginId = "co.touchlab.swiftkt"
    }

    override val pluginId: String = SwiftKtCommandLineProcessor.pluginId

    private val options = listOf(
        Options.swiftPackModule,
        Options.swiftSourceFile,
        Options.expandedSwiftDir,
    )
    private val optionsMap = options.associateBy { it.optionName }
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

        val swiftSourceFile = PluginOption(
            optionName = "swiftSourceFile",
            valueDescription = "<absolute path>",
            description = "",
            allowMultipleOccurrences = true,
            serialize = File::getAbsolutePath,
            deserialize = ::File,
        )

        val expandedSwiftDir = PluginOption(
            optionName = "expandedSwiftDir",
            valueDescription = "<absolute path>",
            description = "",
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
            Options.swiftSourceFile -> {
                configuration.add(ConfigurationKeys.swiftSourceFiles, Options.swiftSourceFile.deserialize(value))
            }
            Options.expandedSwiftDir -> {
                configuration.putIfNotNull(ConfigurationKeys.expandedSwiftDir, Options.expandedSwiftDir.deserialize(value))
            }
        }
    }
}


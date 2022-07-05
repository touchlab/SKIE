package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File

@AutoService(CommandLineProcessor::class)
class SwiftPackConfigCommandLineProcessor: CommandLineProcessor {
    companion object {
        const val pluginId = "co.touchlab.swiftpack.config"
    }

    override val pluginId: String = SwiftPackConfigCommandLineProcessor.pluginId

    private val options = listOf(
        CliOption(
            optionName = Options.outputDir,
            valueDescription = "path",
            description = "Path to store the Swift template files in.",
        )
    )
    override val pluginOptions: Collection<AbstractCliOption> = options

    object Options {
        const val outputDir = "outputDir"
    }

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (option.optionName) {
            Options.outputDir -> configuration.putIfNotNull(SwiftPackConfigurationKeys.outputDir, File(value))
        }
    }
}

@AutoService(ComponentRegistrar::class)
class SwiftPackConfigComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val outputDir = configuration.get(SwiftPackConfigurationKeys.outputDir)
        SwiftPackModuleBuilder.Config.outputDir = outputDir
    }
}
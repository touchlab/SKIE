package co.touchlab.skie.devsupport.irinspector.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfiguration

enum class IrInspectorPluginOption {
    Output {

        override val option: CliOption = CliOption("output", "Path to an output file", "")

        override fun processValue(value: String, configuration: CompilerConfiguration) {
            IrInspectorPluginCompilerConfiguration.OutputPath.put(value, configuration)
        }
    };

    val optionName: String
        get() = option.optionName

    abstract val option: CliOption

    private fun matches(cliOption: AbstractCliOption): Boolean =
        cliOption.optionName == option.optionName

    abstract fun processValue(value: String, configuration: CompilerConfiguration)

    companion object {

        val all: List<CliOption> = values().map { it.option }

        operator fun get(cliOption: AbstractCliOption): IrInspectorPluginOption =
            values().firstOrNull { it.matches(cliOption) }
                ?: throw IllegalArgumentException("Unsupported Cli option ${cliOption.optionName}")
    }
}

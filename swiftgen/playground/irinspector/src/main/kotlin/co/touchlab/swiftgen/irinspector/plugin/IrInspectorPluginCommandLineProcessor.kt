package co.touchlab.swiftgen.irinspector.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class IrInspectorPluginCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = Companion.pluginId

    override val pluginOptions: Collection<AbstractCliOption> = IrInspectorPluginOption.all

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        IrInspectorPluginOption[option].processValue(value, configuration)
    }

    companion object {

        const val pluginId: String = "co.touchlab.swiftgen.irinspector.plugin"
    }
}

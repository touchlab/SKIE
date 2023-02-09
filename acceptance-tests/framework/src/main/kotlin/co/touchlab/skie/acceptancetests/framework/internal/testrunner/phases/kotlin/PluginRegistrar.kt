package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class PluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        configure.get().invoke(configuration)

        plugins.get().forEach {
            it.invoke(this, configuration)
        }
    }

    companion object {

        val plugins: ThreadLocal<List<ExtensionStorage.(configuration: CompilerConfiguration) -> Unit>> = ThreadLocal()
        val configure: ThreadLocal<CompilerConfiguration.() -> Unit> = ThreadLocal()
    }
}

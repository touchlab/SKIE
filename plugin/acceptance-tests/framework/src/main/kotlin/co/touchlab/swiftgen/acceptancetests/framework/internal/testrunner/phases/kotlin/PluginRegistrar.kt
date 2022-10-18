package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

internal class PluginRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configure.get().invoke(configuration, project)

        plugins.get().forEach {
            it.registerProjectComponents(project, configuration)
        }
    }

    companion object {

        val plugins: ThreadLocal<List<ComponentRegistrar>> = ThreadLocal()
        val configure: ThreadLocal<CompilerConfiguration.(project: MockProject) -> Unit> = ThreadLocal()
    }
}
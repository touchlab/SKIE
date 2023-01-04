package co.touchlab.skie.test

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar

internal class PluginRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: org.jetbrains.kotlin.config.CompilerConfiguration) {
        configure.get().invoke(configuration, project)

        plugins.get().forEach {
            it.registerProjectComponents(project, configuration)
        }
    }

    companion object {

        val plugins: ThreadLocal<List<ComponentRegistrar>> = ThreadLocal()
        val configure: ThreadLocal<org.jetbrains.kotlin.config.CompilerConfiguration.(project: MockProject) -> Unit> = ThreadLocal()
    }
}

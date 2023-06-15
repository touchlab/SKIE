package co.touchlab.skie.plugin.shim

import org.gradle.api.Plugin
import org.gradle.api.Project

actual class ShimTestImpl actual constructor(): ShimTest {
    actual override val hello: String = "Hello"

    actual override fun world(): ShimTest {
        return this
    }
}

actual abstract class ShimPluginTest: Plugin<Project> {
    override fun apply(project: Project) {
        println("Hello from ShimPluginTest - K1.8.0!")
    }
}

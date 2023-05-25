package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.configuration.SkieExtension
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class SkieGradlePluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("co.touchlab.skie")

        assert(project.extensions.getByName("skie") is SkieExtension)
    }
}

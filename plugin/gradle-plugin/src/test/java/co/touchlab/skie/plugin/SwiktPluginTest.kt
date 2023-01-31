package co.touchlab.skie.plugin

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class SwiktPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("co.touchlab.skie")

        assert(project.extensions.getByName("skie") is SkieExtension)
    }
}

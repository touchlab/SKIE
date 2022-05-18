package co.touchlab.swikt.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SwiktPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("co.touchlab.swikt")

        assert(project.extensions.getByName("swikt") is SwiktExtension)
    }
}

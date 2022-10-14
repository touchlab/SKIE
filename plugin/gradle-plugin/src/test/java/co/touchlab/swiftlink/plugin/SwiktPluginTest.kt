package co.touchlab.swiftlink.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SwiktPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("co.touchlab.swiftlink")

        assert(project.extensions.getByName("swiftlink") is SwiftLinkExtension)
    }
}

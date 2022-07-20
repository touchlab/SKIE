package co.touchlab.swiftkt.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SwiktPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("co.touchlab.swiftkt")

        assert(project.extensions.getByName("swiftkt") is SwiftKtExtension)
    }
}

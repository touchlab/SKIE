package co.touchlab.skie.gradle.publish

import com.gradle.publish.PublishPlugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class SkiePublishGradlePlugin : BaseJavaSkiePublishPlugin() {

    override fun apply(target: Project) {
        super.apply(target)

        applyGradlePublishingPlugin(target)
        configurePublication(target)
    }

    private fun applyGradlePublishingPlugin(target: Project) {
        target.plugins.apply(PublishPlugin::class.java)
    }

    private fun configurePublication(target: Project) {
        target.afterEvaluate {
            target.extensions.configure(PublishingExtension::class.java) {
                val publication = publications.getByName("pluginMaven") as MavenPublication

                publication.artifactId = target.mavenArtifactId
            }
        }
    }
}

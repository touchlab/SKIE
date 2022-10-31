package co.touchlab.skie.gradle.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class SkiePublishMultiplatformPlugin : BaseSkiePublishPlugin() {

    override fun apply(target: Project) {
        super.apply(target)

        configurePublication(target)
    }

    private fun configurePublication(target: Project) {
        target.afterEvaluate {
            target.extensions.configure(PublishingExtension::class.java) {
                publications.all {
                    (this as? MavenPublication)?.apply {
                        artifactId = if (name == "kotlinMultiplatform") target.mavenArtifactId else "${target.mavenArtifactId}-$name"
                    }
                }
            }
        }
    }
}

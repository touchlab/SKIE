package co.touchlab.skie.gradle.publish

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class SkiePublishJvmPlugin : BaseJavaSkiePublishPlugin() {

    override fun afterKotlinPlugin(target: Project) {
        super.afterKotlinPlugin(target)

        configurePublication(target)
    }

    private fun configurePublication(target: Project) {
        target.extensions.configure(PublishingExtension::class.java) {
            publications {
                create("maven", MavenPublication::class.java) {
                    artifactId = target.mavenArtifactId

                    from(target.components.getAt("java"))
                }
            }
        }
    }
}

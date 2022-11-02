package co.touchlab.skie.gradle.publish

import com.gradle.publish.PublishPlugin
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class SkiePublishGradlePlugin : BaseJavaSkiePublishPlugin() {

    override fun apply(target: Project) {
        super.apply(target)

        with(target) {
            applyGradlePublishingPlugin()
            configurePublication()
        }
    }

    private fun Project.applyGradlePublishingPlugin() {
        plugins.apply(PublishPlugin::class.java)
    }

    private fun Project.configurePublication() {
        afterEvaluate {
            extensions.configure(PublishingExtension::class.java) {
                configureMainPublicationName(publications)
            }
        }
    }

    private fun Project.configureMainPublicationName(publications: PublicationContainer) {
        val mainPublication = publications.first { it.isMainPublication }
        val markerPublications = publications.filterNot { it.isMainPublication }

        renameMainPublication(mainPublication)
        markerPublications.forEach {
            fixDependencyInMarkerPublication(it)
        }
    }

    private val Publication.isMainPublication: Boolean
        get() = this.name == "pluginMaven"

    private fun Project.renameMainPublication(publication: Publication) {
        require(publication is MavenPublication) { "Only Maven publications are supported." }

        publication.artifactId = mavenArtifactId
    }

    private fun Project.fixDependencyInMarkerPublication(publication: Publication) {
        require(publication is MavenPublication) { "Only Maven publications are supported." }

        publication.pom.withXml {
            this.asElement().getElementsByTagName("dependencies").forEach { dependencies ->
                dependencies.getElementsByTagName("artifactId").forEach { artifactId ->
                    artifactId.textContent = mavenArtifactId
                }
            }
        }
    }

    private inline fun NodeList.forEach(action: (Element) -> Unit) {
        for (i in 0 until this.length) {
            action(this.item(i) as Element)
        }
    }
}

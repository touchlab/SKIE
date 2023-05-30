package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.opensource.OpenSourceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File
import kotlin.reflect.KClass

internal class OpenSourceAnalyticsProducer(
    private val project: Project,
) : AnalyticsProducer<AnalyticsFeature.OpenSource> {

    override val featureType: KClass<AnalyticsFeature.OpenSource> = AnalyticsFeature.OpenSource::class

    override val name: String = "openSource"

    override fun produce(configuration: AnalyticsFeature.OpenSource): ByteArray =
        OpenSourceAnalytics(
            rootProjectName = project.rootProject.name,
            projectFullName = "${project.rootProject.name}${project.path}",
            gitRemotes = project.getGitRemotes(),
        ).encode()
}

private fun OpenSourceAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()

internal fun Project.getGitRemotes(): List<String> {
    val directoryWithGit = project.projectDir.findGitRoot() ?: return emptyList()

    val git = Git.open(directoryWithGit)

    return git.remoteList().call()
        .flatMap { it.urIs }
        .filter { it.isRemote }
        .map { it.host + "/" + it.path }
}

private tailrec fun File.findGitRoot(): File? =
    if (resolve(".git").exists()) this else parentFile?.findGitRoot()

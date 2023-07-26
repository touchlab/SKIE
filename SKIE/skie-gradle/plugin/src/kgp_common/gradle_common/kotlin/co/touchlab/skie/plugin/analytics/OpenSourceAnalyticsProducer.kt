package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.opensource.OpenSourceAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

internal class OpenSourceAnalyticsProducer(
    private val project: Project,
) : AnalyticsProducer {

    override val feature: SkieFeature = SkieFeature.Analytics_OpenSource

    override val name: String = "openSource"

    override fun produce(): String =
        OpenSourceAnalytics(
            rootProjectName = project.rootProject.name,
            projectFullName = "${project.rootProject.name}${project.path}",
            gitRemotes = project.getGitRemotes(),
        ).serialize()
}

private fun OpenSourceAnalytics.serialize(): String =
    Json.encodeToString(this)

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

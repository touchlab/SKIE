package co.touchlab.skie.plugin.analytics.git

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import java.io.File

internal data class IdentifyingGitAnalytics(
    val gitRemotes: List<String>,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val feature: SkieFeature = SkieFeature.Analytics_Identifying_Git

        override val name: String = "identifying-git"

        override fun produce(): String =
            IdentifyingGitAnalytics(
                gitRemotes = getGitRemotes(),
            ).toPrettyJson()

        private fun getGitRemotes(): List<String> =
            project.getGit()
                ?.remoteList()
                ?.call()
                ?.flatMap { it.urIs }
                ?.filter { it.isRemote }
                ?.map { it.host + "/" + it.path }
                ?: emptyList()
    }
}

internal fun Project.getGit(): Git? =
    projectDir.findGitRoot()?.let { Git.open(it) }

private tailrec fun File.findGitRoot(): File? =
    if (resolve(".git").exists()) this else parentFile?.findGitRoot()

package co.touchlab.skie.plugin.generator.internal.analytics.git

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.directory.SkieBuildDirectory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import java.io.File

internal class GitRemotesAnalyticsProducer(private val skieBuildDirectory: SkieBuildDirectory) : AnalyticsProducer {

    override val feature: SkieFeature = SkieFeature.Analytics_GitRemotes

    override val name: String = "git-remotes"

    override fun produce(): String =
        GitRemotesAnalytics(
            gitRemotes = getGitRemotes(),
        ).serialize()

    private fun getGitRemotes(): List<String> {
        val directoryWithGit = skieBuildDirectory.directory.findGitRoot() ?: return emptyList()

        val git = Git.open(directoryWithGit)

        return git.remoteList().call()
            .flatMap { it.urIs }
            .filter { it.isRemote }
            .map { it.host + "/" + it.path }
    }

    private tailrec fun File.findGitRoot(): File? =
        if (resolve(".git").exists()) this else parentFile?.findGitRoot()
}

private val json = Json { prettyPrint = true }

private fun GitRemotesAnalytics.serialize(): String =
    json.encodeToString(this)

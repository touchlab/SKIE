package co.touchlab.skie.plugin.analytics.git

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.PersonIdent
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File
import java.text.Normalizer

data class GitAnalytics(
    val numberOfContributors: Int,
    val numberOfCommits: Int,
    val numberOfBranches: Int,
    val numberOfTags: Int,
) {

    class Producer(
        private val gitRoot: Provider<File>,
    ) : AnalyticsProducer {

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Git

        override val name: String = "git"

        override fun produce(): String {
            val git = gitRoot.orNull?.let { Git.open(it) } ?: return """{ "error": "git not found" }"""

            return GitAnalytics(
                numberOfContributors = git.log().all().call().map { it.authorIdent.normalizedName }.distinct().size,
                numberOfCommits = git.log().all().call().count(),
                numberOfBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().size,
                numberOfTags = git.tagList().call().size,
            ).toPrettyJson()
        }
    }
}

private val PersonIdent.normalizedName: String
    get() = Normalizer.normalize(name, Normalizer.Form.NFC)

fun Project.getGitRoot(): Provider<File> = provider {
    projectDir.findGitRoot()
}

private tailrec fun File.findGitRoot(): File? =
    if (resolve(".git").exists()) this else parentFile?.findGitRoot()

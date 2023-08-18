package co.touchlab.skie.plugin.analytics.git

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.lib.PersonIdent
import org.gradle.api.Project
import java.text.Normalizer

internal data class AnonymousGitAnalytics(
    val numberOfContributors: Int,
    val numberOfCommits: Int,
    val numberOfBranches: Int,
    val numberOfTags: Int,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Anonymous_Git

        override val name: String = "anonymous-git"

        override fun produce(): String {
            val git = project.getGit() ?: return """{ "error": "git not found" }"""

            return AnonymousGitAnalytics(
                numberOfContributors = git.log().all().call().map { it.authorIdent.normalizedName }.distinct().size,
                numberOfCommits = git.log().all().call().count(),
                numberOfBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().size,
                numberOfTags = git.tagList().call().size
            ).toPrettyJson()
        }
    }
}

private val PersonIdent.normalizedName: String
    get() = Normalizer.normalize(name, Normalizer.Form.NFC)

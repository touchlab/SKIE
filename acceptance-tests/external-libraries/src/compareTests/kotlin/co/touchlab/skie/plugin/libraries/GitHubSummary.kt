package co.touchlab.skie.plugin.libraries

import java.io.File

class GitHubSummary(
    private val summaryFile: File?,
) {
    private val appendScope = AppendScope()

    fun appendSection(title: String? = null, sectionBlock: AppendScope.() -> Unit) {
        appendLine("")
        title?.let {
            appendLine(it)
            appendLine("")
        }
        appendScope.sectionBlock()
        appendLine("")
    }

    fun appendLine(message: String) {
        summaryFile?.appendText(message + "\n")
        println(message)
    }

    companion object {
        fun create(): GitHubSummary {
            return System.getenv("GITHUB_STEP_SUMMARY")
                ?.let(::File)
                ?.takeIf { it.exists() }
                .let(::GitHubSummary)
        }
    }

    inner class AppendScope {
        operator fun String.unaryPlus() {
            appendLine(this)
        }
    }
}

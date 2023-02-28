package co.touchlab.skie.api.phases.header

import co.touchlab.skie.api.phases.SkieLinkingPhase
import java.io.File

abstract class BaseHeaderModificationPhase(private val headerFile: File) : SkieLinkingPhase {

    override fun execute() {
        val content = headerFile.readLines()

        val modifiedContent = modifyHeaderContent(content)

        val mergedContent = modifiedContent.dropLastWhile { it.isBlank() }.joinToString("\n", postfix = "\n")

        headerFile.writeText(mergedContent)
    }

    protected abstract fun modifyHeaderContent(content: List<String>): List<String>
}

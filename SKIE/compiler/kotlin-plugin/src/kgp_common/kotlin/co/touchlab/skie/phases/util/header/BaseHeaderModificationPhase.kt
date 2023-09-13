package co.touchlab.skie.phases.util.header

import co.touchlab.skie.phases.SirPhase

abstract class BaseHeaderModificationPhase : SirPhase {

    context(SirPhase.Context)
    final override fun execute() {
        val content = framework.kotlinHeader.readLines()

        val modifiedContent = modifyHeaderContent(content)

        val mergedContent = modifiedContent.dropLastWhile { it.isBlank() }.joinToString("\n", postfix = "\n")

        framework.kotlinHeader.writeText(mergedContent)
    }

    protected abstract fun modifyHeaderContent(content: List<String>): List<String>
}

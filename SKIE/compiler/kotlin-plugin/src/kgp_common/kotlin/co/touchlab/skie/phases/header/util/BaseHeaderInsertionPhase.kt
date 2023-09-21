package co.touchlab.skie.phases.header.util

abstract class BaseHeaderInsertionPhase : BaseHeaderModificationPhase() {

    override fun modifyHeaderContent(content: List<String>): List<String> {
        val insertIndex = content.indexOfFirst { insertImmediatelyBefore(it) }

        return content.take(insertIndex) + insertedContent + listOf("") + content.drop(insertIndex)
    }

    protected abstract val insertedContent: List<String>

    protected abstract fun insertImmediatelyBefore(line: String): Boolean
}

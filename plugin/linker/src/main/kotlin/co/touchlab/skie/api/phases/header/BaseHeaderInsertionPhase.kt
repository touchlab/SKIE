package co.touchlab.skie.api.phases.header

import java.io.File

abstract class BaseHeaderInsertionPhase(headerFile: File) : BaseHeaderModificationPhase(headerFile) {

    override fun modifyHeaderContent(content: List<String>): List<String> {
        val insertIndex = content.indexOfFirst { isInsertionPoint(it) }

        return content.take(insertIndex) + insertedContent + listOf("") + content.drop(insertIndex)
    }

    protected abstract val insertedContent: List<String>

    protected abstract fun isInsertionPoint(line: String): Boolean
}

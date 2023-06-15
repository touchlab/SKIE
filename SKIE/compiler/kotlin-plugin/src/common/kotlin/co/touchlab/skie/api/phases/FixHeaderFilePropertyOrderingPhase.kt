package co.touchlab.skie.api.phases

import co.touchlab.skie.api.phases.header.BaseHeaderModificationPhase
import java.io.File

class FixHeaderFilePropertyOrderingPhase(headerFile: File) : BaseHeaderModificationPhase(headerFile) {

    override fun modifyHeaderContent(content: List<String>): List<String> {
        val reorderedContent = mutableListOf<String>()

        val iterator = content.iterator()
        reorderedContent.addReorderedHeaderContent(iterator)

        return reorderedContent
    }

    private fun MutableList<String>.addReorderedHeaderContent(iterator: Iterator<String>) {
        while (iterator.hasNext()) {
            val line = iterator.next()

            this.add(line)

            if (line.isInterfaceDeclarationStart) {
                this.addReorderedInterfaceDeclaration(iterator)
            }
        }
    }

    private fun MutableList<String>.addReorderedInterfaceDeclaration(iterator: Iterator<String>) {
        val propertyDeclarations = mutableListOf<String>()
        val otherDeclarations = mutableListOf<String>()

        while (iterator.hasNext()) {
            val line = iterator.next()

            when {
                line.isInterfaceDeclarationEnd -> {
                    otherDeclarations.add(line)
                    break
                }
                line.isPropertyDeclaration -> propertyDeclarations.add(line)
                else -> otherDeclarations.add(line)
            }
        }

        this.addAll(propertyDeclarations)
        this.addAll(otherDeclarations)
    }

    private val String.isInterfaceDeclarationStart: Boolean
        get() = this.startsWith("@interface ")

    private val String.isPropertyDeclaration: Boolean
        get() = this.startsWith("@property ")

    private val String.isInterfaceDeclarationEnd: Boolean
        get() = this.startsWith("@end")
}

package co.touchlab.skie.phases.header

import co.touchlab.skie.oir.element.renderForwardDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.header.util.BaseHeaderModificationPhase

class FixForwardDeclarationsPhase(
    private val context: SirPhase.Context,
) : BaseHeaderModificationPhase() {

    override fun modifyHeaderContent(content: List<String>): List<String> {
        val forwardDeclarationSectionEnd = content.indexOfFirst { it.startsWith("NS_ASSUME_NONNULL_BEGIN") }

        return content.mapIndexedNotNull { index: Int, line: String ->
            if (index >= forwardDeclarationSectionEnd) return@mapIndexedNotNull line

            when {
                line.startsWith("@class ") -> getClassForwardDeclarations()
                line.startsWith("@protocol ") -> getProtocolForwardDeclarations()
                else -> line
            }
        }
    }

    private fun getClassForwardDeclarations(): String? {
        val classes = context.oirProvider.allClasses
        if (classes.isEmpty()) {
            return null
        }

        return "@class " + classes.joinToString(", ") { it.renderForwardDeclaration() } + ";"
    }

    private fun getProtocolForwardDeclarations(): String? {
        val protocols = context.oirProvider.allProtocols
        if (protocols.isEmpty()) {
            return null
        }

        return "@protocol " + protocols.joinToString(", ") { it.renderForwardDeclaration() } + ";"
    }
}

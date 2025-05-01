package co.touchlab.skie.phases.header

import co.touchlab.skie.oir.element.renderForwardDeclaration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.header.util.BaseHeaderModificationPhase

class FixForwardDeclarationsPhase(private val context: SirPhase.Context) : BaseHeaderModificationPhase() {

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

        // The sorting by name length should prevent a bug in the Swift compiler where it confuses Obj-C name and Swift name which leads to compilation errors.
        // This can happen if there are two types and the name of the first type is in the form `$FrameworkName$OtherTypeName`.
        // In that case the Swift name of this type collides with the Obj-C name of the other type due to Kotlin adding the prefix of the framework name.
        // For example, this forward declaration order works:
        //     ```
        //     @protocol SharedSharedFlow; // Is SharedFlow in Kotlin and Swift
        //     @protocol SharedFlow;       // Is Flow in Kotlin and Swift
        //     ```
        // but this does not:
        //     ```
        //     @protocol SharedFlow;
        //     @protocol SharedSharedFlow;
        //     ```
        return "@class " + classes.sortedByDescending { it.name }.joinToString(", ") { it.renderForwardDeclaration() } + ";"
    }

    private fun getProtocolForwardDeclarations(): String? {
        val protocols = context.oirProvider.allProtocols
        if (protocols.isEmpty()) {
            return null
        }

        return "@protocol " + protocols.sortedByDescending { it.name }.joinToString(", ") { it.renderForwardDeclaration() } + ";"
    }
}

package co.touchlab.skie.phases.header

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.header.util.BaseHeaderInsertionPhase
import co.touchlab.skie.phases.header.util.HeaderDeclarationsProvider

class AddForwardDeclarationsPhase(
    private val context: SirPhase.Context,
) : BaseHeaderInsertionPhase() {

    private lateinit var classes: Set<String>
    private lateinit var protocols: Set<String>

    override val insertedContent: List<String>
        get() {
            return listOfNotNull(
                "@class ${classes.joinToString(", ")};".takeIf { classes.isNotEmpty() },
                "",
                "@protocol ${protocols.joinToString(", ")};".takeIf { protocols.isNotEmpty() },
            )
        }

    override fun modifyHeaderContent(content: List<String>): List<String> {
        classes = getNewDeclarations(
            forwardDeclarations = context.headerDeclarationsProvider.forwardlyDeclaredClasses,
            definedDeclarations = context.headerDeclarationsProvider.definedClasses,
            referencedDeclarations = context.objCTypeRenderer.mappedClasses,
        )
        protocols = getNewDeclarations(
            forwardDeclarations = context.headerDeclarationsProvider.forwardlyDeclaredProtocols,
            definedDeclarations = context.headerDeclarationsProvider.definedProtocols,
            referencedDeclarations = context.objCTypeRenderer.mappedProtocols,
        )

        return super.modifyHeaderContent(content)
    }

    private fun getNewDeclarations(
        forwardDeclarations: Set<HeaderDeclarationsProvider.Declaration>,
        definedDeclarations: Set<HeaderDeclarationsProvider.Declaration>,
        referencedDeclarations: Set<String>,
    ): Set<String> {
        val forwardDeclarationsNames = forwardDeclarations.map { it.name }.toSet()

        val knownDeclarations = forwardDeclarations + definedDeclarations

        val missingExternalReferencedDeclarations = referencedDeclarations.filterNotIn(knownDeclarations)
        val definedDeclarationsWithoutForwardDeclaration = definedDeclarations.filter { it.name !in forwardDeclarationsNames }.map { it.toString() }

        return missingExternalReferencedDeclarations + definedDeclarationsWithoutForwardDeclaration
    }

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun Set<String>.filterNotIn(declarations: Set<HeaderDeclarationsProvider.Declaration>): Set<String> =
    this - declarations.map { it.name }.toSet()

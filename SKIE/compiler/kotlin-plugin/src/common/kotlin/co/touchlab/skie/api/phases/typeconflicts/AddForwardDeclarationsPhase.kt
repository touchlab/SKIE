package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.header.BaseHeaderInsertionPhase
import java.io.File

class AddForwardDeclarationsPhase(
    headerFile: File,
    private val objCTypeRenderer: ObjCTypeRenderer,
) : BaseHeaderInsertionPhase(headerFile) {

    private val classRegex = "^@interface ([^ <,]+(<[^>]*>)?).*".toRegex()
    private val protocolRegex = "^@protocol ([^ ,]+).*".toRegex()

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
        val classesFromHeader = classRegex.parseDeclarations(content)
        val protocolsFromHeader = protocolRegex.parseDeclarations(content)

        classes = (classesFromHeader + objCTypeRenderer.mappedClasses.filterExistingClasses(classesFromHeader)).toSet()
        protocols = (protocolsFromHeader + objCTypeRenderer.mappedProtocols).toSet()

        return super.modifyHeaderContent(content)
    }

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun Collection<String>.filterExistingClasses(classes: List<String>): List<String> =
    (this - classes.map { it.stripTypeParameters() }.toSet())

private fun String.stripTypeParameters(): String =
    substringBefore("<")

private fun Regex.parseDeclarations(content: List<String>): List<String> =
    content.mapNotNull { parseDeclaration(it) }

private fun Regex.parseDeclaration(line: String): String? =
    matchEntire(line)?.groupValues?.get(1)

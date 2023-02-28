package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.header.BaseHeaderInsertionPhase
import java.io.File

class AddForwardDeclarationsPhase(
    headerFile: File,
) : BaseHeaderInsertionPhase(headerFile) {

    private val classRegex = "^@interface ([^ <,]+(<[^>]*>)?).*".toRegex()
    private val protocolRegex = "^@protocol ([^ ,]+).*".toRegex()

    private lateinit var classes: List<String>
    private lateinit var protocols: List<String>

    override val insertedContent: List<String>
        get() {
            return listOfNotNull(
                "@class ${classes.joinToString(", ")};".takeIf { classes.isNotEmpty() },
                "",
                "@protocol ${protocols.joinToString(", ")};".takeIf { protocols.isNotEmpty() },
            )
        }

    override fun modifyHeaderContent(content: List<String>): List<String> {
        classes = classRegex.parseDeclarations(content)
        protocols = protocolRegex.parseDeclarations(content)

        return super.modifyHeaderContent(content)
    }

    override fun isInsertionPoint(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun Regex.parseDeclarations(content: List<String>): List<String> =
    content.mapNotNull { parseDeclaration(it) }.distinct()

private fun Regex.parseDeclaration(line: String): String? =
    matchEntire(line)?.groupValues?.get(1)

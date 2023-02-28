package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.header.BaseHeaderInsertionPhase
import java.io.File

class AddTypeDefPhase(
    kotlinHeader: File,
    private val objCTypeMapper: ObjCTypeRenderer,
) : BaseHeaderInsertionPhase(kotlinHeader) {

    override val insertedContent: List<String>
        get() = objCTypeMapper.typedefs.map { it.createTypeDef() }

    override fun isInsertionPoint(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun ObjCTypeRenderer.Mapping.createTypeDef(): String =
    "typedef $from $to __attribute__((__swift_private__));"

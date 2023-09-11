package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.util.header.BaseHeaderInsertionPhase
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import java.io.File

class AddTypeDefPhase(
    kotlinHeader: File,
    private val objCTypeMapper: ObjCTypeRenderer,
) : BaseHeaderInsertionPhase(kotlinHeader) {

    override val insertedContent: List<String>
        get() = objCTypeMapper.typedefs.map { it.createTypeDef() }

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun ObjCTypeRenderer.Mapping.createTypeDef(): String =
    "typedef $from $to __attribute__((__swift_private__));"

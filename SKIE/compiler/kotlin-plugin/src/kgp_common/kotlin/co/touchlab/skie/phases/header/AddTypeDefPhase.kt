package co.touchlab.skie.phases.header

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.header.util.BaseHeaderInsertionPhase
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer

class AddTypeDefPhase(
    private val context: SirPhase.Context,
) : BaseHeaderInsertionPhase() {

    override val insertedContent: List<String>
        get() = context.objCTypeRenderer.typedefs.map { it.createTypeDef() }

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun ObjCTypeRenderer.Mapping.createTypeDef(): String =
    "typedef $from $to __attribute__((__swift_private__));"

package co.touchlab.skie.phases.header

import co.touchlab.skie.oir.element.OirTypeDef
import co.touchlab.skie.oir.element.OirVisibility
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.header.util.BaseHeaderInsertionPhase

class AddTypeDefPhase(
    private val context: SirPhase.Context,
) : BaseHeaderInsertionPhase() {

    override val insertedContent: List<String>
        get() = context.oirProvider.allFiles
            .flatMap { it.declarations.filterIsInstance<OirTypeDef>() }
            .map { it.render() }

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}

private fun OirTypeDef.render(): String =
    "typedef ${type.render()} ${name}${visibility.attributeSuffixIfNeeded};"

private val OirVisibility.attributeSuffixIfNeeded: String
    get() = if (this == OirVisibility.Private) " __attribute__((__swift_private__))" else ""

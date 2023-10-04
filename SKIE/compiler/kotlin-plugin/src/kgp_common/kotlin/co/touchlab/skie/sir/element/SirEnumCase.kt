package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirEnumCaseParent

class SirEnumCase(
    var simpleName: String,
    parent: SirClass,
) : SirElement {

    var parent: SirClass by sirEnumCaseParent(parent)

    val associatedValues: MutableList<SirEnumCaseAssociatedValue> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}: $simpleName"

    companion object {

        context(SirClass)
        operator fun invoke(
            simpleName: String,
        ): SirEnumCase =
            SirEnumCase(
                simpleName = simpleName,
                parent = this@SirClass,
            )
    }
}

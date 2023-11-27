package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirEnumCaseParent

class SirEnumCase(
    var simpleName: String,
    parent: SirClass,
) : SirElement {

    var parent: SirClass by sirEnumCaseParent(parent)

    val associatedValues: MutableList<SirEnumCaseAssociatedValue> = mutableListOf()

    val index: Int
        get() = parent.enumCases.indexOf(this)

    override fun toString(): String = "${this::class.simpleName}: $simpleName"

    fun toReadableString(): String =
        "case " + parent.fqName.toString() + "." + simpleName

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

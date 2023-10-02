package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.element.util.sirDeclarationParent

class SirEnumCase(
    var simpleName: String,
    parent: SirDeclarationParent,
) : SirDeclaration {

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    val associatedValues: MutableList<SirEnumCaseAssociatedValue> = mutableListOf()

    override fun toString(): String =
        "${this::class.simpleName}: $simpleName"

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            simpleName: String,
        ): SirEnumCase =
            SirEnumCase(
                simpleName = simpleName,
                parent = this@SirDeclarationParent,
            )
    }
}

package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent

class SirExtension(
    var classDeclaration: SirClass,
    parent: SirTopLevelDeclarationParent,
    override var visibility: SirVisibility = SirVisibility.Public,
) : SirDeclaration, SirDeclarationNamespace {

    override var parent: SirTopLevelDeclarationParent by sirDeclarationParent(parent)

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    val conditionalConstraints: MutableList<SirConditionalConstraint> = mutableListOf()

    override val fqName: SirFqName
        get() = classDeclaration.fqName

    override fun toString(): String =
        "${this::class.simpleName}: $fqName"

    companion object {

        context(SirTopLevelDeclarationParent)
        operator fun invoke(
            classDeclaration: SirClass,
            visibility: SirVisibility = SirVisibility.Public,
        ): SirExtension =
            SirExtension(
                classDeclaration = classDeclaration,
                parent = this@SirTopLevelDeclarationParent,
                visibility = visibility,
            )
    }
}

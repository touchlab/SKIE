package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent

// Can be instantiated via SirProvider to improve performance.
class SirExtension(
    override val classDeclaration: SirClass,
    parent: SirTopLevelDeclarationParent,
) : SirDeclaration, SirDeclarationNamespace {

    // Do not add support for SirVisibility, there are many assumptions made on the fact that extensions do not modify visibility of the content inside.

    override val parent: SirTopLevelDeclarationParent by sirDeclarationParent(parent)

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    val conditionalConstraints: MutableList<SirConditionalConstraint> = mutableListOf()

    override val fqName: SirFqName
        get() = classDeclaration.fqName

    override fun toString(): String =
        "${this::class.simpleName}: $fqName"
}

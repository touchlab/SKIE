package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirDeclaredSirType

// Can be instantiated via SirProvider to improve performance.
class SirExtension(
    override val classDeclaration: SirClass,
    parent: SirTopLevelDeclarationParent,
    superTypes: List<SirDeclaredSirType> = emptyList(),
    attributes: List<String> = emptyList(),
) : SirDeclaration, SirDeclarationNamespace, SirDeclarationWithSuperTypes, SirElementWithAttributes, SirConditionalConstraintParent {

    // Do not add support for SirVisibility, there are many assumptions made on the fact that extensions do not modify visibility of the content inside.

    override val parent: SirTopLevelDeclarationParent by sirDeclarationParent(parent)

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val conditionalConstraints: MutableList<SirConditionalConstraint> = mutableListOf()

    override var documentation: String = ""

    override val superTypes: MutableList<SirDeclaredSirType> = superTypes.toMutableList()

    override val fqName: SirFqName
        get() = classDeclaration.fqName

    override fun toString(): String =
        "${this::class.simpleName}: $fqName"

    companion object {

        context(SirTopLevelDeclarationParent)
        operator fun invoke(
            classDeclaration: SirClass,
            attributes: List<String> = emptyList(),
            superTypes: List<SirDeclaredSirType> = emptyList(),
        ): SirExtension =
            SirExtension(
                classDeclaration = classDeclaration,
                parent = this@SirTopLevelDeclarationParent,
                superTypes = superTypes,
                attributes = attributes,
            )
    }
}

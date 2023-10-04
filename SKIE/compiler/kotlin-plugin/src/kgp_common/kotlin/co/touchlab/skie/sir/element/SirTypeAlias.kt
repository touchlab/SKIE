package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirType

class SirTypeAlias(
    override var baseName: String,
    parent: SirDeclarationParent,
    override var visibility: SirVisibility = SirVisibility.Public,
    typeFactory: ((SirTypeAlias) -> SirType),
) : SirTypeDeclaration, SirTypeParameterParent {

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    override val defaultType: DeclaredSirType by lazy {
        toType(emptyList())
    }

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    var type: SirType = typeFactory(this)

    override val isHashable: Boolean
        get() = type.isHashable

    override val isPrimitive: Boolean
        get() = type.isPrimitive

    val framework: SirModule
        get() = parent.module

    override val originalFqName: SirFqName = fqName

    override val publicName: SirFqName
        get() = fqName

    override val internalName: SirFqName
        get() = fqName

    override fun toString(): String = "${this::class.simpleName}: $fqName"

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            baseName: String,
            visibility: SirVisibility = SirVisibility.Public,
            typeFactory: ((SirTypeAlias) -> SirType),
        ): SirTypeAlias =
            SirTypeAlias(
                baseName = baseName,
                parent = this@SirDeclarationParent,
                visibility = visibility,
                typeFactory = typeFactory,
            )
    }
}

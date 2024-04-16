package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.TypeDefOirType

class OirTypeDef(
    override val name: String,
    val type: OirType,
    override val parent: OirTopLevelDeclarationParent,
    override val visibility: OirVisibility = OirVisibility.Public,
) : OirTypeDeclaration {

    init {
        parent.declarations.add(this)
    }

    override val defaultType: TypeDefOirType by lazy {
        toType()
    }

    override fun toType(typeArguments: List<OirType>): TypeDefOirType =
        TypeDefOirType(this, typeArguments = typeArguments)

    override fun toType(vararg typeArguments: OirType): TypeDefOirType =
        toType(typeArguments.toList())

    override fun toString(): String = "${this::class.simpleName}: $name"
}

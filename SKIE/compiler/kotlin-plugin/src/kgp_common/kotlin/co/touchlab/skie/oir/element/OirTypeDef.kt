package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.OirType

class OirTypeDef(
    override val name: String,
    val type: OirType,
    override val parent: OirTopLevelDeclarationParent,
    override val visibility: OirVisibility = OirVisibility.Public,
) : OirTypeDeclaration {

    override val defaultType: DeclaredOirType by lazy {
        toType(emptyList())
    }

    init {
        parent.declarations.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $name"
}

package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType

sealed interface OirTypeDeclaration : OirTopLevelDeclaration {

    val name: String

    val visibility: OirVisibility

    val defaultType: OirType

    fun toType(typeArguments: List<OirType>): OirType

    fun toType(vararg typeArguments: OirType): OirType
}

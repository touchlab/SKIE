package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.OirType

sealed interface OirTypeDeclaration : OirTopLevelDeclaration {

    val name: String

    val defaultType: DeclaredOirType

    fun toType(typeArguments: List<OirType>): DeclaredOirType =
        DeclaredOirType(this, typeArguments = typeArguments)

    fun toType(vararg typeArguments: OirType): DeclaredOirType =
        toType(typeArguments.toList())
}

package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.type.SirDeclaredSirType

sealed interface SirDeclarationWithSuperTypes : SirDeclaration {
    val superTypes: MutableList<SirDeclaredSirType>
}

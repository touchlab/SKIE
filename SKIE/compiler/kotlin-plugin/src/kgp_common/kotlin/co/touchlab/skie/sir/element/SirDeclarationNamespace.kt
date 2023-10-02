package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName

sealed interface SirDeclarationNamespace : SirDeclarationParent {

    val fqName: SirFqName
}


package co.touchlab.skie.plugin.api.sir.element

import co.touchlab.skie.plugin.api.sir.SirFqName

sealed interface SirDeclarationNamespace : SirDeclarationParent {

    val fqName: SirFqName
}

package co.touchlab.skie.kir.element

import co.touchlab.skie.sir.element.SirDeclarationWithVisibility
import co.touchlab.skie.sir.element.isExported

sealed interface KirBridgeableDeclaration<T : SirDeclarationWithVisibility> {

    val originalSirDeclaration: T

    val primarySirDeclaration: T

    var bridgedSirDeclaration: T?
}

val <T : SirDeclarationWithVisibility> KirBridgeableDeclaration<T>.associatedSirDeclarations: List<T>
    get() = listOfNotNull(originalSirDeclaration, bridgedSirDeclaration)

val <T : SirDeclarationWithVisibility> KirBridgeableDeclaration<T>.associatedExportedSirDeclarations: List<T>
    get() = associatedSirDeclarations.filter { it.isExported }

inline fun <T : SirDeclarationWithVisibility> KirBridgeableDeclaration<T>.forEachAssociatedExportedSirDeclaration(action: (T) -> Unit) {
    associatedExportedSirDeclarations.forEach(action)
}

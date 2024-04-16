package co.touchlab.skie.kir.element

import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.isExported

sealed interface KirBridgeableDeclaration<T : SirDeclaration> {

    val originalSirDeclaration: T

    val primarySirDeclaration: T

    var bridgedSirDeclaration: T?
}

val <T : SirDeclaration> KirBridgeableDeclaration<T>.associatedSirDeclarations: List<T>
    get() = listOfNotNull(originalSirDeclaration, bridgedSirDeclaration)

val <T : SirDeclaration> KirBridgeableDeclaration<T>.associatedExportedSirDeclarations: List<T>
    get() = associatedSirDeclarations.filter { it.isExported }

inline fun <T : SirDeclaration> KirBridgeableDeclaration<T>.forEachAssociatedExportedSirDeclaration(action: (T) -> Unit) {
    associatedExportedSirDeclarations.forEach(action)
}

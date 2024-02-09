package co.touchlab.skie.oir.element

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.kir.element.DeprecationLevel

sealed interface OirCallableDeclaration : OirElement {

    val parent: OirCallableDeclarationParent

    val owner: OirClass
        get() = when (val parent = parent) {
            is OirClass -> parent
            is OirExtension -> parent.classDeclaration
        }

    val scope: OirScope

    val deprecationLevel: DeprecationLevel

    // TODO This is likely not needed
    val visibility: OirVisibility
        get() = originalSirCallableDeclaration.visibility.toOirVisibility()

    val primarySirCallableDeclaration: SirCallableDeclaration
        get() = bridgedSirCallableDeclaration ?: originalSirCallableDeclaration

    val originalSirCallableDeclaration: SirCallableDeclaration

    val bridgedSirCallableDeclaration: SirCallableDeclaration?
}

package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.CallableDeclarationConfiguration
import co.touchlab.skie.oir.element.OirCallableDeclaration
import co.touchlab.skie.sir.element.SirCallableDeclaration

sealed interface KirCallableDeclaration<S : SirCallableDeclaration> :
    KirElement,
    KirBridgeableDeclaration<S> {

    val owner: KirClass

    val origin: Origin

    val scope: KirScope

    val modality: Modality

    val deprecationLevel: DeprecationLevel

    val configuration: CallableDeclarationConfiguration

    val oirCallableDeclaration: OirCallableDeclaration

    val primarySirCallableDeclaration: SirCallableDeclaration
        get() = oirCallableDeclaration.primarySirCallableDeclaration

    val originalSirCallableDeclaration: SirCallableDeclaration
        get() = oirCallableDeclaration.originalSirCallableDeclaration

    val bridgedSirCallableDeclaration: SirCallableDeclaration?
        get() = oirCallableDeclaration.bridgedSirCallableDeclaration

    val isRefinedInSwift: Boolean

    val kotlinSignature: String

    val module: KirModule
        get() = owner.module

    enum class Origin {

        Member,
        Extension,
        Global,
    }

    enum class Modality {
        Final,
        Open,
        Abstract,
    }
}

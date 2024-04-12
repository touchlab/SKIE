package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.CallableDeclarationConfiguration
import co.touchlab.skie.oir.element.OirCallableDeclaration
import co.touchlab.skie.sir.element.SirCallableDeclaration

sealed interface KirCallableDeclaration<S : SirCallableDeclaration> : KirElement, KirBridgeableDeclaration<S> {

    val owner: KirClass

    val origin: Origin

    val scope: KirScope

    val deprecationLevel: DeprecationLevel

    val configuration: CallableDeclarationConfiguration

    val oirCallableDeclaration: OirCallableDeclaration

    val isRefinedInSwift: Boolean

    val kotlinSignature: String

    val module: KirModule
        get() = owner.module

    enum class Origin {

        Member, Extension, Global
    }
}

package co.touchlab.skie.kir.element

import co.touchlab.skie.kir.configuration.KirConfiguration
import co.touchlab.skie.oir.element.OirCallableDeclaration
import co.touchlab.skie.sir.element.SirCallableDeclaration
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

sealed interface KirCallableDeclaration<S : SirCallableDeclaration> : KirElement, KirBridgeableDeclaration<S> {

    val baseDescriptor: CallableMemberDescriptor

    val descriptor: CallableMemberDescriptor

    val owner: KirClass

    val origin: Origin

    val scope: KirScope

    val deprecationLevel: DeprecationLevel

    val configuration: KirConfiguration

    val oirCallableDeclaration: OirCallableDeclaration

    val module: KirModule
        get() = owner.module

    enum class Origin {

        Member, Extension, Global
    }
}

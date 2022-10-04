package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinMemberParent<ID: KotlinMemberParent.Id>: KotlinSymbol<ID> {
    @Serializable
    sealed interface Id: KotlinSymbol.Id
}

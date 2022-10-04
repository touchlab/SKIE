package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinType<ID: KotlinType.Id>: KotlinMemberParent<ID> {

    @Serializable
    sealed interface Id: KotlinMemberParent.Id
}

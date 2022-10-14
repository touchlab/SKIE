package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinTypeReference<ID: KotlinTypeReference.Id>: KotlinMemberParentReference<ID> {

    @Serializable
    sealed interface Id: KotlinMemberParentReference.Id
}

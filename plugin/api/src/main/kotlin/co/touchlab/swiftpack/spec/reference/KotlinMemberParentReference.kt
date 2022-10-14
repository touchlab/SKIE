package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinMemberParentReference<ID: KotlinMemberParentReference.Id>: KotlinDeclarationReference<ID> {
    @Serializable
    sealed interface Id: KotlinDeclarationReference.Id
}

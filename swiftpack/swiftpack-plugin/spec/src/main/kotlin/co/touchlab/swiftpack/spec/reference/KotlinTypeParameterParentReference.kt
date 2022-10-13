package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinTypeParameterParentReference<ID: KotlinTypeParameterParentReference.Id>: KotlinDeclarationReference<ID> {
    @Serializable
    sealed interface Id: KotlinDeclarationReference.Id
}

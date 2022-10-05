package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinCallableMemberReference<ID: KotlinCallableMemberReference.Id>: KotlinDeclarationReference<ID>,
    KotlinTypeParameterParentReference<ID> {
    sealed interface Id: KotlinDeclarationReference.Id, KotlinTypeParameterParentReference.Id
}

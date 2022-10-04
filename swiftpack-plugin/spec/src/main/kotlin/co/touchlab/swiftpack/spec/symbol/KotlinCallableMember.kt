package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinCallableMember<ID: KotlinCallableMember.Id>: KotlinSymbol<ID>, KotlinTypeParameterParent<ID> {
    sealed interface Id: KotlinSymbol.Id, KotlinTypeParameterParent.Id
}

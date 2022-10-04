package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable

@Serializable
sealed interface KotlinTypeParameterParent<ID: KotlinTypeParameterParent.Id>: KotlinSymbol<ID> {
    @Serializable
    sealed interface Id: KotlinSymbol.Id
}

package co.touchlab.swiftpack.spec.symbol

import kotlinx.serialization.Serializable

@Serializable
data class KotlinFile(
    val id: Id,
) {
    @Serializable
    data class Id(val value: String): KotlinSymbol.Id
}

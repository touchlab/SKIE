package co.touchlab.swiftpack.spec.reference

import kotlinx.serialization.Serializable

@Serializable
data class KotlinFileReference(
    val id: Id,
) {
    @Serializable
    data class Id(val value: String): KotlinDeclarationReference.Id
}

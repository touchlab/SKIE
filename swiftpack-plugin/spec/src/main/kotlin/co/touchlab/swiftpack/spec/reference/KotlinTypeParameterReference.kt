package co.touchlab.swiftpack.spec.reference

import co.touchlab.swiftpack.spec.signature.IdSignatureSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
data class KotlinTypeParameterReference(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinTypeReference<KotlinTypeParameterReference.Id> {
    @Serializable
    data class Id(val value: String): KotlinTypeReference.Id
}

package co.touchlab.swiftpack.spec.reference

import co.touchlab.swiftpack.spec.signature.IdSignatureSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
data class KotlinPropertyReference(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinCallableMemberReference<KotlinPropertyReference.Id> {

    @Serializable
    data class Id(val value: String): KotlinCallableMemberReference.Id
}

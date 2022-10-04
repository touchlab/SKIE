package co.touchlab.swiftpack.spec.symbol

import co.touchlab.swiftpack.spec.signature.IdSignatureSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
data class KotlinProperty(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinCallableMember<KotlinProperty.Id> {

    @Serializable
    data class Id(val value: String): KotlinCallableMember.Id
}

package co.touchlab.swiftpack.spec.symbol

import co.touchlab.swiftpack.spec.signature.IdSignatureSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

data class KotlinPackage(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinMemberParent<KotlinPackage.Id> {
    @Serializable
    data class Id(val value: String): KotlinMemberParent.Id
}

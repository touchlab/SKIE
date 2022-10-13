package co.touchlab.swiftpack.spec.reference

import co.touchlab.swiftpack.spec.signature.IdSignatureSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

data class KotlinPackageReference(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinMemberParentReference<KotlinPackageReference.Id> {
    @Serializable
    data class Id(val value: String): KotlinMemberParentReference.Id
}

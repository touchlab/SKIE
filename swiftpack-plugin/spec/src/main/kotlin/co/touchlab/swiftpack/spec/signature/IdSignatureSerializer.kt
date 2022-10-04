package co.touchlab.swiftpack.spec.signature

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.kotlin.ir.util.IdSignature

object IdSignatureSerializer: KSerializer<IdSignature> {
    private val wrappedSerializer = KotlinIdSignature.serializer()

    override val descriptor: SerialDescriptor = wrappedSerializer.descriptor

    override fun deserialize(decoder: Decoder): IdSignature {
        val wrapped = decoder.decodeSerializableValue(wrappedSerializer)
        return wrapped.toIdSignature()
    }

    override fun serialize(encoder: Encoder, value: IdSignature) {
        val wrapped = KotlinIdSignature(value)
        encoder.encodeSerializableValue(wrappedSerializer, wrapped)
    }
}

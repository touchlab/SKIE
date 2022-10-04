package co.touchlab.swiftpack.spec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.kotlin.ir.util.IdSignature

@Serializable
sealed interface KotlinSymbol<ID: KotlinSymbol.Id> {
    val id: ID
    val signature: IdSignature

    @Serializable
    sealed interface Id
}

@Serializable
sealed interface KotlinIdSignature {
    fun toIdSignature(): IdSignature

    @Serializable
    data class AccessorSignature(val propertySignature: KotlinIdSignature, val accessorSignature: CommonSignature): KotlinIdSignature {
        constructor(signature: IdSignature.AccessorSignature): this(
            propertySignature = KotlinIdSignature(signature.accessorSignature),
            accessorSignature = CommonSignature(signature.accessorSignature)
        )

        override fun toIdSignature(): IdSignature = IdSignature.AccessorSignature(
            propertySignature = propertySignature.toIdSignature(),
            accessorSignature = accessorSignature.toIdSignature(),
        )
    }

    @Serializable
    data class CommonSignature(val packageFqName: String, val declarationFqName: String, val id: Long?, val mask: Long): KotlinIdSignature {
        constructor(signature: IdSignature.CommonSignature): this(
            packageFqName = signature.packageFqName,
            declarationFqName = signature.declarationFqName,
            id = signature.id,
            mask = signature.mask,
        )

        override fun toIdSignature(): IdSignature.CommonSignature = IdSignature.CommonSignature(
            packageFqName = packageFqName,
            declarationFqName = declarationFqName,
            id = id,
            mask = mask
        )
    }

    @Serializable
    data class CompositeSignature(val container: KotlinIdSignature, val inner: KotlinIdSignature): KotlinIdSignature {
        constructor(signature: IdSignature.CompositeSignature): this(
            container = KotlinIdSignature(signature.container),
            inner = KotlinIdSignature(signature.inner),
        )

        override fun toIdSignature(): IdSignature.CompositeSignature = IdSignature.CompositeSignature(
            container = container.toIdSignature(),
            inner = inner.toIdSignature(),
        )
    }

    @Serializable
    data class FileLocalSignature(val container: KotlinIdSignature, val id: Long, val description: String? = null): KotlinIdSignature {
        constructor(signature: IdSignature.FileLocalSignature): this(
            container = KotlinIdSignature(signature.container),
            id = signature.id,
            description = signature.description,
        )

        override fun toIdSignature(): IdSignature.FileLocalSignature = IdSignature.FileLocalSignature(
            container = container.toIdSignature(),
            id = id,
            description = description,
        )
    }

    // @Serializable
    // data class FileSignature(val packageFqName: String, val filePath: String): KotlinIdSignature {
    //     constructor(signature: IdSignature.FileSignature): this(
    //         packageFqName = signature.packageFqName,
    //         filePath = signature.filePath,
    //     )
    //
    //     override fun toIdSignature(): IdSignature.FileSignature = IdSignature.FileSignature(
    //         packageFqName = packageFqName,
    //         filePath = filePath,
    //     )
    // }

    @Serializable
    data class LocalSignature(val localFqn: String, val hashSig: Long?, val description: String?): KotlinIdSignature {
        constructor(signature: IdSignature.LocalSignature): this(
            localFqn = signature.localFqn,
            hashSig = signature.hashSig,
            description = signature.description,
        )

        override fun toIdSignature(): IdSignature.LocalSignature = IdSignature.LocalSignature(
            localFqn = localFqn,
            hashSig = hashSig,
            description = description,
        )
    }

    @Serializable
    data class LoweredDeclarationSignature(val original: KotlinIdSignature, val stage: Int, val index: Int): KotlinIdSignature {
        constructor(signature: IdSignature.LoweredDeclarationSignature): this(
            original = KotlinIdSignature(signature.original),
            stage = signature.stage,
            index = signature.index,
        )

        override fun toIdSignature(): IdSignature.LoweredDeclarationSignature = IdSignature.LoweredDeclarationSignature(
            original = original.toIdSignature(),
            stage = stage,
            index = index,
        )
    }

    @Serializable
    data class ScopeLocalDeclarationSignature(val id: Int): KotlinIdSignature {
        constructor(signature: IdSignature.ScopeLocalDeclaration): this(
            id = signature.id,
        )

        override fun toIdSignature(): IdSignature.ScopeLocalDeclaration = IdSignature.ScopeLocalDeclaration(
            id = id,
        )
    }

    @Serializable
    data class SpecialFakeOverrideSignature(
        val memberSignature: KotlinIdSignature,
        val overriddenSignatures: List<KotlinIdSignature>,
    ): KotlinIdSignature {
        constructor(signature: IdSignature.SpecialFakeOverrideSignature): this(
            memberSignature = KotlinIdSignature(signature.memberSignature),
            overriddenSignatures = signature.overriddenSignatures.map { KotlinIdSignature(it) },
        )

        override fun toIdSignature(): IdSignature.SpecialFakeOverrideSignature = IdSignature.SpecialFakeOverrideSignature(
            memberSignature = memberSignature.toIdSignature(),
            overriddenSignatures = overriddenSignatures.map { it.toIdSignature() },
        )
    }
}

fun KotlinIdSignature(signature: IdSignature): KotlinIdSignature {
    return when (signature) {
        is IdSignature.AccessorSignature -> KotlinIdSignature.AccessorSignature(signature)
        is IdSignature.CommonSignature -> KotlinIdSignature.CommonSignature(signature)
        is IdSignature.CompositeSignature -> KotlinIdSignature.CompositeSignature(signature)
        is IdSignature.FileLocalSignature -> KotlinIdSignature.FileLocalSignature(signature)
        is IdSignature.FileSignature -> TODO() // KotlinIdSignature.FileSignature(signature)
        is IdSignature.LocalSignature -> KotlinIdSignature.LocalSignature(signature)
        is IdSignature.LoweredDeclarationSignature -> KotlinIdSignature.LoweredDeclarationSignature(signature)
        is IdSignature.ScopeLocalDeclaration -> KotlinIdSignature.ScopeLocalDeclarationSignature(signature)
        is IdSignature.SpecialFakeOverrideSignature -> KotlinIdSignature.SpecialFakeOverrideSignature(signature)
    }
}

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

// TODO: KotlinFile isn't a KotlinSymbol, but we need it to be for Transforms. Might find a better name to avoid confusion.
@Serializable
data class KotlinFile(
    val id: Id,
) {
    @Serializable
    data class Id(val value: String): KotlinSymbol.Id
}

@Serializable
sealed interface KotlinMemberParent<ID: KotlinMemberParent.Id>: KotlinSymbol<ID> {
    @Serializable
    sealed interface Id: KotlinSymbol.Id
}

@Serializable
sealed interface KotlinCallableMember<ID: KotlinCallableMember.Id>: KotlinSymbol<ID>, KotlinTypeParameterParent<ID> {
    sealed interface Id: KotlinSymbol.Id, KotlinTypeParameterParent.Id
}

@Serializable
sealed interface KotlinTypeParameterParent<ID: KotlinTypeParameterParent.Id>: KotlinSymbol<ID> {
    @Serializable
    sealed interface Id: KotlinSymbol.Id
}

@Serializable
data class KotlinProperty(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinCallableMember<KotlinProperty.Id> {

    @Serializable
    data class Id(val value: String): KotlinCallableMember.Id
}

@Serializable
data class KotlinFunction(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinCallableMember<KotlinFunction.Id> {

    @Serializable
    data class Id(val value: String): KotlinCallableMember.Id
}

@Serializable
sealed interface KotlinType<ID: KotlinType.Id>: KotlinMemberParent<ID> {

    @Serializable
    sealed interface Id: KotlinMemberParent.Id
}

@Serializable
data class KotlinClass(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinType<KotlinClass.Id>, KotlinTypeParameterParent<KotlinClass.Id> {

    @Serializable
    data class Id(val value: String): KotlinType.Id, KotlinTypeParameterParent.Id
}

@Serializable
data class KotlinTypeParameter(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinType<KotlinTypeParameter.Id> {
    @Serializable
    data class Id(val value: String): KotlinType.Id
}

@Serializable
data class KotlinEnumEntry(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinSymbol<KotlinEnumEntry.Id> {
    @Serializable
    data class Id(val value: String): KotlinSymbol.Id
}

data class KotlinPackage(
    override val id: Id,
    @Serializable(with = IdSignatureSerializer::class)
    override val signature: IdSignature,
): KotlinMemberParent<KotlinPackage.Id> {
    @Serializable
    data class Id(val value: String): KotlinMemberParent.Id
}

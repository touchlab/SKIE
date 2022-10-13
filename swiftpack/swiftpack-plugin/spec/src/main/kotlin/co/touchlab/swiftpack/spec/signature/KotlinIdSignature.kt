package co.touchlab.swiftpack.spec.signature

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.ir.util.IdSignature

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

    // TODO: FileSignature is nto supported because it requires an instance of "Any" to initialize and we don't know how to (de)serialize it
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
        is IdSignature.FileSignature -> TODO("FileSignature is not supported") // KotlinIdSignature.FileSignature(signature)
        is IdSignature.LocalSignature -> KotlinIdSignature.LocalSignature(signature)
        is IdSignature.LoweredDeclarationSignature -> KotlinIdSignature.LoweredDeclarationSignature(signature)
        is IdSignature.ScopeLocalDeclaration -> KotlinIdSignature.ScopeLocalDeclarationSignature(signature)
        is IdSignature.SpecialFakeOverrideSignature -> KotlinIdSignature.SpecialFakeOverrideSignature(signature)
    }
}

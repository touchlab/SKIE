package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.internal.InternalReferenceContext
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import org.jetbrains.kotlin.backend.konan.serialization.KonanIdSignaturer
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.KotlinMangler
import org.jetbrains.kotlin.types.KotlinType

internal class DefaultReferenceContext(
    private val compatibleMode: Boolean = true
): InternalReferenceContext {
    private val irMangler: KotlinMangler.IrMangler
        get() = KonanManglerIr

    private val descriptorMangler: KotlinMangler.DescriptorMangler
        get() = KonanManglerDesc

    private val signaturer = KonanIdSignaturer(descriptorMangler)

    private val mutableReferences = mutableMapOf<KotlinDeclarationReference.Id, KotlinDeclarationReference<*>>()
    override val references: Map<KotlinDeclarationReference.Id, KotlinDeclarationReference<*>>
        get() = mutableReferences

    override val symbols: List<KotlinDeclarationReference<*>>
        get() = references.values.toList()

    override fun IrClass.reference(): KotlinClassReference = with(irMangler) {
        getReference(KotlinClassReference.Id(mangleString(compatibleMode))) { id ->
            KotlinClassReference(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrProperty.reference(): KotlinPropertyReference = with(irMangler) {
        getReference(KotlinPropertyReference.Id(mangleString(compatibleMode))) { id ->
            KotlinPropertyReference(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrFunction.reference(): KotlinFunctionReference = with(irMangler) {
        getReference(KotlinFunctionReference.Id(mangleString(compatibleMode))) { id ->
            KotlinFunctionReference(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrEnumEntry.reference(): KotlinEnumEntryReference = with (irMangler) {
        getReference(KotlinEnumEntryReference.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntryReference(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Enum entry ${this@reference} has no signature"
                },
            )
        }
    }

    override fun ClassDescriptor.classReference(): KotlinClassReference = with (descriptorMangler) {
        getReference(KotlinClassReference.Id(mangleString(compatibleMode))) { id ->
            KotlinClassReference(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@classReference)) {
                    "Class ${this@classReference} has no signature"
                },
            )
        }
    }

    override fun ClassDescriptor.enumEntryReference(): KotlinEnumEntryReference = with(descriptorMangler) {
        getReference(KotlinEnumEntryReference.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntryReference(
                id = id,
                signature = requireNotNull(signaturer.composeEnumEntrySignature(this@enumEntryReference)) {
                    "Enum entry ${this@enumEntryReference} has no signature"
                },
            )
        }
    }

    override fun KotlinType.reference(): KotlinTypeReference<*> = with(descriptorMangler) {
        val declarationDescriptor = requireNotNull(constructor.declarationDescriptor) {
            "Type $this has no declaration descriptor"
        }
        getReference(KotlinClassReference.Id(declarationDescriptor.mangleString(compatibleMode))) { id ->
            KotlinClassReference(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(declarationDescriptor)) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun PropertyDescriptor.reference(): KotlinPropertyReference = with(descriptorMangler) {
        getReference(KotlinPropertyReference.Id(mangleString(compatibleMode))) { id ->
            KotlinPropertyReference(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun FunctionDescriptor.reference(): KotlinFunctionReference = with(descriptorMangler) {
        getReference(KotlinFunctionReference.Id(mangleString(compatibleMode))) { id ->
            KotlinFunctionReference(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    private fun <S: KotlinDeclarationReference<ID>, ID: KotlinDeclarationReference.Id> getReference(id: ID, symbolFactory: (ID) -> S): S {
        @Suppress("UNCHECKED_CAST")
        return mutableReferences.getOrPut(id) {
            symbolFactory(id)
        } as S
    }
}

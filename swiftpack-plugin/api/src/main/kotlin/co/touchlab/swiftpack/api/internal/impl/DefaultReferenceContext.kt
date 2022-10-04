package co.touchlab.swiftpack.api.internal.impl

import co.touchlab.swiftpack.api.internal.InternalReferenceContext
import co.touchlab.swiftpack.spec.symbol.KotlinClass
import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import co.touchlab.swiftpack.spec.symbol.KotlinSymbol
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

    private val mutableReferences = mutableMapOf<KotlinSymbol.Id, KotlinSymbol<*>>()
    override val references: Map<KotlinSymbol.Id, KotlinSymbol<*>>
        get() = mutableReferences

    override val symbols: List<KotlinSymbol<*>>
        get() = references.values.toList()

    override fun IrClass.reference(): KotlinClass = with(irMangler) {
        getReference(KotlinClass.Id(mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrProperty.reference(): KotlinProperty = with(irMangler) {
        getReference(KotlinProperty.Id(mangleString(compatibleMode))) { id ->
            KotlinProperty(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrFunction.reference(): KotlinFunction = with(irMangler) {
        getReference(KotlinFunction.Id(mangleString(compatibleMode))) { id ->
            KotlinFunction(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    override fun IrEnumEntry.reference(): KotlinEnumEntry = with (irMangler) {
        getReference(KotlinEnumEntry.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntry(
                id = id,
                signature = requireNotNull(symbol.signature) {
                    "Enum entry ${this@reference} has no signature"
                },
            )
        }
    }

    override fun ClassDescriptor.classReference(): KotlinClass = with (descriptorMangler) {
        getReference(KotlinClass.Id(mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@classReference)) {
                    "Class ${this@classReference} has no signature"
                },
            )
        }
    }

    override fun ClassDescriptor.enumEntryReference(): KotlinEnumEntry = with(descriptorMangler) {
        getReference(KotlinEnumEntry.Id(mangleString(compatibleMode))) { id ->
            KotlinEnumEntry(
                id = id,
                signature = requireNotNull(signaturer.composeEnumEntrySignature(this@enumEntryReference)) {
                    "Enum entry ${this@enumEntryReference} has no signature"
                },
            )
        }
    }

    override fun KotlinType.reference(): co.touchlab.swiftpack.spec.symbol.KotlinType<*> = with(descriptorMangler) {
        val declarationDescriptor = requireNotNull(constructor.declarationDescriptor) {
            "Type $this has no declaration descriptor"
        }
        getReference(KotlinClass.Id(declarationDescriptor.mangleString(compatibleMode))) { id ->
            KotlinClass(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(declarationDescriptor)) {
                    "Class ${this@reference} has no signature"
                },
            )
        }
    }

    override fun PropertyDescriptor.reference(): KotlinProperty = with(descriptorMangler) {
        getReference(KotlinProperty.Id(mangleString(compatibleMode))) { id ->
            KotlinProperty(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Property ${this@reference} has no signature"
                },
            )
        }
    }

    override fun FunctionDescriptor.reference(): KotlinFunction = with(descriptorMangler) {
        getReference(KotlinFunction.Id(mangleString(compatibleMode))) { id ->
            KotlinFunction(
                id = id,
                signature = requireNotNull(signaturer.composeSignature(this@reference)) {
                    "Function ${this@reference} has no signature"
                },
            )
        }
    }

    private fun <S: KotlinSymbol<ID>, ID: KotlinSymbol.Id> getReference(id: ID, symbolFactory: (ID) -> S): S {
        @Suppress("UNCHECKED_CAST")
        return mutableReferences.getOrPut(id) {
            symbolFactory(id)
        } as S
    }
}

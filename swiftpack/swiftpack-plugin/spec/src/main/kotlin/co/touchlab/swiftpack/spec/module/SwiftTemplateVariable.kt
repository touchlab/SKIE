package co.touchlab.swiftpack.spec.module

import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import co.touchlab.swiftpack.spec.reference.KotlinFunctionReference
import co.touchlab.swiftpack.spec.reference.KotlinPropertyReference
import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import co.touchlab.swiftpack.spec.reference.KotlinTypeReference
import kotlinx.serialization.Serializable

@Serializable
sealed interface SwiftTemplateVariable<REFERENCE_ID: KotlinDeclarationReference.Id> {
    val name: Name
    val referenceId: REFERENCE_ID

    @Serializable
    class TypeReference(
        override val name: Name,
        val type: KotlinTypeReference.Id,
    ) : SwiftTemplateVariable<KotlinTypeReference.Id> {
        override val referenceId: KotlinTypeReference.Id
            get() = type
    }

    @Serializable
    class PropertyReference(
        override val name: Name,
        val property: KotlinPropertyReference.Id,
    ): SwiftTemplateVariable<KotlinPropertyReference.Id> {
        override val referenceId: KotlinPropertyReference.Id
            get() = property
    }

    @Serializable
    class FunctionReference(
        override val name: Name,
        val function: KotlinFunctionReference.Id
    ): SwiftTemplateVariable<KotlinFunctionReference.Id> {
        override val referenceId: KotlinFunctionReference.Id
            get() = function
    }

    @Serializable
    class EnumEntryReference(
        override val name: Name,
        val enumEntry: KotlinEnumEntryReference.Id,
    ): SwiftTemplateVariable<KotlinEnumEntryReference.Id> {
        override val referenceId: KotlinEnumEntryReference.Id
            get() = enumEntry
    }

    @JvmInline
    @Serializable
    value class Name(val value: String)
}

package co.touchlab.swiftpack.spec.module

import co.touchlab.swiftpack.spec.symbol.KotlinEnumEntry
import co.touchlab.swiftpack.spec.symbol.KotlinFunction
import co.touchlab.swiftpack.spec.symbol.KotlinProperty
import co.touchlab.swiftpack.spec.symbol.KotlinSymbol
import co.touchlab.swiftpack.spec.symbol.KotlinType
import kotlinx.serialization.Serializable

@Serializable
sealed interface SwiftTemplateVariable<SYMBOL_ID: KotlinSymbol.Id> {
    val name: Name
    val symbol: SYMBOL_ID

    @Serializable
    class TypeReference(
        override val name: Name,
        val type: KotlinType.Id,
    ) : SwiftTemplateVariable<KotlinType.Id> {
        override val symbol: KotlinType.Id
            get() = type
    }

    @Serializable
    class PropertyReference(
        override val name: Name,
        val property: KotlinProperty.Id,
    ): SwiftTemplateVariable<KotlinProperty.Id> {
        override val symbol: KotlinProperty.Id
            get() = property
    }

    @Serializable
    class FunctionReference(
        override val name: Name,
        val function: KotlinFunction.Id
    ): SwiftTemplateVariable<KotlinFunction.Id> {
        override val symbol: KotlinFunction.Id
            get() = function
    }

    @Serializable
    class EnumEntryReference(
        override val name: Name,
        val enumEntry: KotlinEnumEntry.Id,
    ): SwiftTemplateVariable<KotlinEnumEntry.Id> {
        override val symbol: KotlinEnumEntry.Id
            get() = enumEntry
    }

    @JvmInline
    @Serializable
    value class Name(val value: String)
}

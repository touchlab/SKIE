package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class SwiftPackModule(
    val name: String,
    val references: References,
    val files: List<TemplateFile>,
    val kobjcTransforms: KobjcTransforms,
) {
    companion object {
        private val json = Json {
            allowStructuredMapKeys = true
        }

        fun read(file: File): SwiftPackModule {
            return file.inputStream().use {
                json.decodeFromStream(serializer(), it)
            }
        }

        fun SwiftPackModule.write(file: File) {
            file.outputStream().use {
                json.encodeToStream(serializer(), this, it)
            }
        }
    }

    @Serializable
    data class TemplateFile(
        val name: String,
        val contents: String,
    )

    @Serializable
    data class References(
        val types: Map<SwiftPackReference, KotlinTypeReference>,
        val properties: Map<SwiftPackReference, KotlinPropertyReference>,
        val functions: Map<SwiftPackReference, KotlinFunctionReference>,
        val enumEntries: Map<SwiftPackReference, KotlinEnumEntryReference>,
    )
}

@Serializable
data class SwiftPackModule2(
    val name: Name,
    val templateVariables: List<SwiftTemplateVariable<*>>,
    val symbols: List<KotlinSymbol<*>>,
    val files: List<TemplateFile>,
    val transforms: List<ApiTransform>,
) {
    fun namespaced(namespace: String): SwiftPackModule2 {
        return copy(
            name = Name.Namespaced(namespace = namespace, name = name),
        )
    }

    @Serializable
    data class TemplateFile(
        val name: String,
        val contents: String,
    )

    @Serializable
    sealed interface Name {
        @Serializable
        data class Simple(val name: String) : Name

        @Serializable
        data class Namespaced(val namespace: String, val name: Name): Name
    }

    companion object {
        private val json = Json {
            allowStructuredMapKeys = true
            classDiscriminator = "@type"
        }

        fun read(file: File): SwiftPackModule2 {
            return file.inputStream().use {
                json.decodeFromStream(serializer(), it)
            }
        }

        fun SwiftPackModule2.write(file: File) {
            file.outputStream().use {
                json.encodeToStream(serializer(), this, it)
            }
        }
    }
}

const val SWIFTPACK_TEMPLATE_VARIABLE_PREFIX = "__swiftpack__"

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

@Serializable
sealed interface ApiTransform {
    @Serializable
    data class FileTransform(
        val fileId: KotlinFile.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: TypeTransform.Rename.Action? = null,
        val bridge: String? = null,
    ): ApiTransform

    @Serializable
    data class TypeTransform(
        val typeId: KotlinType.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: Rename? = null,
        val bridge: Bridge? = null,
    ): ApiTransform {
        @Serializable
        data class Rename(val kind: Kind, val action: Action) {
            @Serializable
            enum class Kind {
                ABSOLUTE, RELATIVE
            }

            @Serializable
            sealed interface Action {
                @Serializable
                class Prefix(val prefix: String) : Action
                @Serializable
                class Suffix(val suffix: String) : Action
                @Serializable
                class Replace(val newName: String) : Action
            }
        }

        @Serializable
        sealed interface Bridge {
            @Serializable
            data class Absolute(val swiftType: String): Bridge
            @Serializable
            data class Relative(val parentKotlinClass: KotlinClass.Id, val swiftType: String): Bridge
        }
    }

    @Serializable
    class PropertyTransform(
        val propertyId: KotlinProperty.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class FunctionTransform(
        val functionId: KotlinFunction.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform

    @Serializable
    class EnumEntryTransform(
        val enumEntryId: KotlinEnumEntry.Id,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    ): ApiTransform
}

package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable


@Serializable
data class KobjcTransforms(
    val types: Map<KotlinTypeReference, TypeTransform> = emptyMap(),
    val files: Map<KotlinFileReference, FileTransform> = emptyMap(),
    val properties: Map<KotlinPropertyReference, PropertyTransform> = emptyMap(),
    val functions: Map<KotlinFunctionReference, FunctionTransform> = emptyMap(),
    val enumEntries: Map<KotlinEnumEntryReference, EnumEntryTransform> = emptyMap(),
) {
    @Serializable
    data class FileTransform(
        val reference: KotlinFileReference,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
        val bridge: String? = null,
    )

    @Serializable
    data class TypeTransform(
        val reference: KotlinTypeReference,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
        val bridge: String? = null,
        val properties: Map<KotlinPropertyReference, PropertyTransform> = emptyMap(),
        val methods: Map<KotlinFunctionReference, FunctionTransform> = emptyMap(),
        val enumEntries: Map<KotlinEnumEntryReference, EnumEntryTransform> = emptyMap(),
    )

    @Serializable
    class PropertyTransform(
        val reference: KotlinPropertyReference,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    )

    @Serializable
    class FunctionTransform(
        val reference: KotlinFunctionReference,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    )

    @Serializable
    class EnumEntryTransform(
        val reference: KotlinEnumEntryReference,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    )
}

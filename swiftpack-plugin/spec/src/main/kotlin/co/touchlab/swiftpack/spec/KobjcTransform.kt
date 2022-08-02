package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable


@Serializable
data class KobjcTransforms(
    val types: Map<String, TypeTransform> = emptyMap(),
    val properties: Map<String, PropertyTransform> = emptyMap(),
    val functions: Map<String, FunctionTransform> = emptyMap(),
) {
    @Serializable
    data class TypeTransform(
        val type: String,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
        val bridge: String? = null,
        val properties: Map<String, PropertyTransform> = emptyMap(),
        val methods: Map<String, FunctionTransform> = emptyMap(),
    )

    @Serializable
    class PropertyTransform(
        val name: String,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    )

    @Serializable
    class FunctionTransform(
        val name: String,
        val hide: Boolean = false,
        val remove: Boolean = false,
        val rename: String? = null,
    )
}

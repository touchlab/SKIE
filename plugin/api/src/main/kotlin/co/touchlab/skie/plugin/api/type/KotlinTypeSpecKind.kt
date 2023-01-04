package co.touchlab.skie.plugin.api.type

enum class KotlinTypeSpecKind {
    ORIGINAL,
    SWIFT_GENERICS,
    BRIDGED,
}

sealed interface KotlinTypeSpecUsage {
    sealed interface ParameterType: KotlinTypeSpecUsage {
        object Lambda: ParameterType

        companion object: ParameterType
    }

    sealed interface ReturnType: KotlinTypeSpecUsage {
        object Lambda: ReturnType

        object SuspendFunction: ReturnType

        companion object: ReturnType
    }

    sealed interface TypeParam: KotlinTypeSpecUsage {
        object IsReference: TypeParam
        object IsHashable: TypeParam

        object OptionalWrapped: TypeParam

        object ObjcCollectionElement: TypeParam

        object AllowingNullability: TypeParam

        companion object: TypeParam
    }

    companion object: KotlinTypeSpecUsage
}

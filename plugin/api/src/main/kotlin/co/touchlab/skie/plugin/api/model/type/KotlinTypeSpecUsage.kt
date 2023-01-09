package co.touchlab.skie.plugin.api.model.type

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

    object Default: KotlinTypeSpecUsage
}

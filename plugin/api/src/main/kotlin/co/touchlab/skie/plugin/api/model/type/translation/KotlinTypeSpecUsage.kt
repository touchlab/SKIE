package co.touchlab.skie.plugin.api.model.type.translation

sealed interface KotlinTypeSpecUsage {
    sealed interface ParameterType : KotlinTypeSpecUsage {
        sealed interface Lambda : ParameterType {
            object OptionalWrapped : Lambda

            companion object : Lambda
        }

        companion object : ParameterType
    }

    sealed interface ReturnType : KotlinTypeSpecUsage {
        sealed interface Lambda : ReturnType {
            object OptionalWrapped : Lambda

            companion object : Lambda
        }

        sealed interface SuspendFunction : ReturnType {
            object OptionalWrapped : SuspendFunction

            companion object : SuspendFunction
        }

        companion object : ReturnType
    }

    sealed interface TypeParam : KotlinTypeSpecUsage {
        object IsReference : TypeParam
        object IsHashable : TypeParam

        object OptionalWrapped : TypeParam

        object ObjcCollectionElement : TypeParam

        object AllowingNullability : TypeParam

        companion object : TypeParam
    }

    object Default : KotlinTypeSpecUsage
}

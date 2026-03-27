package co.touchlab.skie.kotlingenerator.ir

sealed interface KotlinType {

    fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter> = emptyList()): String

    data class Declared(
        val packageName: String,
        val name: String,
        val safeName: String = name.replace(".", "_"),
    ) : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>) = safeName
    }

    data class Optional(val wrapped: KotlinType) : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>): String =
            "optional_" + wrapped.getSafeName(visitedTypeParameters)
    }

    data class Parametrized(val kotlinType: KotlinType, val typeArguments: List<KotlinType>) : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>): String =
            kotlinType.getSafeName(visitedTypeParameters) + "__" + typeArguments.joinToString("__") {
                it.getSafeName(visitedTypeParameters)
            }
    }

    data class Lambda(
        val isSuspend: Boolean,
        val receiverType: KotlinType?,
        val parameterTypes: List<KotlinType>,
        val returnType: KotlinType,
    ) : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>): String =
            (listOfNotNull("suspend".takeIf { isSuspend }, "lambda", receiverType?.let { "on_${it.getSafeName(visitedTypeParameters)}" }) +
                parameterTypes.map { it.getSafeName(visitedTypeParameters) } +
                listOf(returnType.getSafeName(visitedTypeParameters)))
                .joinToString("___")
    }

    data class TypeParameterUsage(val typeParameter: KotlinTypeParameter) : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>): String =
            if (typeParameter in visitedTypeParameters) {
                "SELF"
            } else {
                typeParameter.name
            }
    }

    data object Star : KotlinType {

        override fun getSafeName(visitedTypeParameters: List<KotlinTypeParameter>): String = "star"
    }
}

val KotlinType.isOptional: Boolean
    get() = this is KotlinType.Optional ||
        (this is KotlinType.TypeParameterUsage && this.typeParameter.bounds.all { it.isOptional })

fun KotlinType.parameterizedBy(vararg parameters: KotlinType): KotlinType.Parametrized =
    KotlinType.Parametrized(this, parameters.toList())

fun KotlinType.Declared.nestedClass(className: String): KotlinType.Declared =
    KotlinType.Declared(this.packageName, this.name + "." + className, this.safeName + "_" + className)

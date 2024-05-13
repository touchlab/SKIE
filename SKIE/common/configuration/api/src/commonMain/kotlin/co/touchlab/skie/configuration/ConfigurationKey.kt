package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.util.throwIfNull

interface ConfigurationKey<T> {

    val name: kotlin.String
        get() {
            val canonicalName = this::class.java.canonicalName
            val packageName = this::class.java.packageName

            return if (packageName.isEmpty()) {
                canonicalName
            } else {
                canonicalName.drop(packageName.length + 1)
            }
        }

    val defaultValue: T

    val skieRuntimeValue: T
        get() = defaultValue

    val isInheritable: kotlin.Boolean
        get() = false

    fun hasAnnotationValue(configurationTarget: ConfigurationTarget): kotlin.Boolean

    fun getAnnotationValue(configurationTarget: ConfigurationTarget): T

    fun deserialize(value: kotlin.String?): T

    fun serialize(value: T): kotlin.String? =
        value?.toString()

    interface NonOptional<T : Any> : ConfigurationKey<T> {

        override fun hasAnnotationValue(configurationTarget: ConfigurationTarget): kotlin.Boolean =
            findAnnotationValue(configurationTarget) != null

        fun findAnnotationValue(configurationTarget: ConfigurationTarget): T?

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): T =
            findAnnotationValue(configurationTarget)
                ?: throw IllegalStateException("Target $configurationTarget does not have an annotation value.")
    }

    interface String : NonOptional<kotlin.String> {

        override fun deserialize(value: kotlin.String?): kotlin.String =
            value.throwIfNull()
    }

    interface Boolean : NonOptional<kotlin.Boolean> {

        override fun deserialize(value: kotlin.String?): kotlin.Boolean =
            value.throwIfNull().toBooleanStrict()
    }

    interface Int : NonOptional<kotlin.Int> {

        override fun deserialize(value: kotlin.String?): kotlin.Int =
            value.throwIfNull().toInt()
    }

    interface OptionalString : ConfigurationKey<kotlin.String?> {

        override fun deserialize(value: kotlin.String?): kotlin.String? = value
    }
}

package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.util.throwIfNull
import kotlin.io.path.Path

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

    fun hasAnnotationValue(configurationTarget: ConfigurationTarget): kotlin.Boolean = false

    fun getAnnotationValue(configurationTarget: ConfigurationTarget): T = throw NotImplementedError()

    fun deserialize(value: kotlin.String?): T

    fun serialize(value: T): kotlin.String? =
        value?.toString()

    interface NonOptional<T : Any> : ConfigurationKey<T> {

        fun findAnnotationValue(configurationTarget: ConfigurationTarget): T? = null

        override fun hasAnnotationValue(configurationTarget: ConfigurationTarget): kotlin.Boolean =
            findAnnotationValue(configurationTarget) != null

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

    interface Path : NonOptional<java.nio.file.Path> {

        override fun deserialize(value: kotlin.String?): java.nio.file.Path =
            Path(value.throwIfNull())
    }

    interface Enum<E : kotlin.Enum<E>> : NonOptional<E> {

        override fun deserialize(value: kotlin.String?): E =
            valueOf(value.throwIfNull())

        override fun serialize(value: E): kotlin.String? =
            value.name

        fun valueOf(value: kotlin.String): E
    }
}

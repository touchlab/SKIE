package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.util.throwIfNull

interface ConfigurationKey<T> {

    val name: kotlin.String

    val defaultValue: T

    fun getAnnotationValue(configurationTarget: ConfigurationTarget): T?

    fun deserialize(value: kotlin.String?): T

    fun serialize(value: T): kotlin.String? =
        value?.toString()

    interface String : ConfigurationKey<kotlin.String> {

        override fun deserialize(value: kotlin.String?): kotlin.String =
            value.throwIfNull()
    }

    interface Boolean : ConfigurationKey<kotlin.Boolean> {

        override fun deserialize(value: kotlin.String?): kotlin.Boolean =
            value.throwIfNull().toBooleanStrict()
    }

    interface Int : ConfigurationKey<kotlin.Int> {

        override fun deserialize(value: kotlin.String?): kotlin.Int =
            value.throwIfNull().toInt()
    }

    interface OptionalString : ConfigurationKey<kotlin.String?> {

        override fun deserialize(value: kotlin.String?): kotlin.String? = value
    }
}

package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.util.throwIfNull
import co.touchlab.skie.configuration.values.ValidationSeverity

object Validation {

    /**
     * The severity of messages thrown by the compiler plugin as a result of validation error.
     */
    object Severity : ConfigurationKey<ValidationSeverity> {

        override val defaultValue: ValidationSeverity = ValidationSeverity.Error

        override val skieRuntimeValue: ValidationSeverity = ValidationSeverity.Error

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): ValidationSeverity? =
            null

        override fun deserialize(value: String?): ValidationSeverity =
            ValidationSeverity.valueOf(value.throwIfNull())
    }
}

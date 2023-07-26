package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.util.throwIfNull

object Validation {

    /**
     * The severity of messages thrown by the compiler plugin as a result of validation error.
     */
    object Severity : ConfigurationKey<Severity.Level> {

        override val defaultValue: Level = Level.Error

        override val skieRuntimeValue: Level = Level.Error

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Level? =
            null

        override fun deserialize(value: String?): Level =
            Level.valueOf(value.throwIfNull())

        enum class Level {
            Error, Warning, None
        }
    }
}

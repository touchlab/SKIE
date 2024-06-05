package co.touchlab.skie.configuration.internal

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationScope

object SwiftCompilerConfigurationKeys {

    object SwiftVersion : ConfigurationKey.String, ConfigurationScope.Global {

        override val defaultValue: String = "5"
    }

    object FreeCompilerArgs : ConfigurationKey.List<String>, ConfigurationScope.Global {

        override val defaultValue: List<String> = emptyList()

        override fun serializeElement(value: String): String = value

        override fun deserializeElement(value: String): String = value
    }
}

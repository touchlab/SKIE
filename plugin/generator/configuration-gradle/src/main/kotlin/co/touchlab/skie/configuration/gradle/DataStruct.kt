package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.annotations.DataStruct
import co.touchlab.skie.configuration.annotations.EnumInterop
import co.touchlab.skie.configuration.hasAnnotation

object DataStruct {

    /**
     * If true, the struct code is generated for the given data class.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<DataStruct.Enabled>() -> true
                configurationTarget.hasAnnotation<DataStruct.Disabled>() -> false
                else -> null
            }
    }
}

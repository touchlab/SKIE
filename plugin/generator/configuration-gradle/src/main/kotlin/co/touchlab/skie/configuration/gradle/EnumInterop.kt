package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.hasAnnotation

object EnumInterop {

    /**
     * If true, the interop code is generated for the given enum.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<EnumInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<EnumInterop.Disabled>() -> false
                else -> null
            }
    }
}

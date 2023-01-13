package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.annotations.SuspendInterop
import co.touchlab.skie.configuration.hasAnnotation

object SuspendInterop {

    /**
     * If true, the interop code is generated for the given suspend function.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<SuspendInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<SuspendInterop.Disabled>() -> false
                else -> null
            }
    }
}

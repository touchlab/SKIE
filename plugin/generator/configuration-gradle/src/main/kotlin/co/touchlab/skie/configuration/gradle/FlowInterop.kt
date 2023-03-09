package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.annotations.FlowInterop
import co.touchlab.skie.configuration.hasAnnotation

object FlowInterop {

    /**
     * If true, the interop code is generated for the given flow.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<FlowInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<FlowInterop.Disabled>() -> false
                else -> null
            }
    }
}

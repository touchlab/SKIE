package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.FlowInterop

object FlowInterop {

    /**
     * If true, the interop code is generated for the given flow.
     */
    object Enabled : ConfigurationKey.Boolean, ConfigurationScope.All {

        override val defaultValue: Boolean = true

        override val skieRuntimeValue: Boolean = false

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<FlowInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<FlowInterop.Disabled>() -> false
                else -> null
            }
    }
}

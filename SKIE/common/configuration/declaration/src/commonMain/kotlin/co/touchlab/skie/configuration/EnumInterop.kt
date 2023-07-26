package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.EnumInterop

object EnumInterop {

    /**
     * If true, the interop code is generated for the given enum.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override val skieRuntimeValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<EnumInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<EnumInterop.Disabled>() -> false
                else -> null
            }
    }
}

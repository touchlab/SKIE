package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.ExperimentalFeatures

object ExperimentalFeatures {

    /**
     * If true, enables experimental features which might not be fully implemented yet which may cause compilation problems.
     */
    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override val skieRuntimeValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<ExperimentalFeatures.Enabled>() -> true
                configurationTarget.hasAnnotation<ExperimentalFeatures.Disabled>() -> false
                else -> null
            }
    }
}

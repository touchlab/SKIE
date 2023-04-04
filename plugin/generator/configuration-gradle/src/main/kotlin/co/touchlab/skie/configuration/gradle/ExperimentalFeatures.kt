package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.annotations.ExperimentalFeatures
import co.touchlab.skie.configuration.hasAnnotation

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

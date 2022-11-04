package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop
import co.touchlab.skie.configuration.findAnnotation
import co.touchlab.skie.configuration.hasAnnotation

object DefaultArgumentInterop {

    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Disabled>() -> false
                else -> null
            }
    }

    object MaximumDefaultArgumentCount : ConfigurationKey.Int {

        override val defaultValue: Int = 5

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Int? =
            configurationTarget.findAnnotation<DefaultArgumentInterop.MaximumDefaultArgumentCount>()?.count
    }
}

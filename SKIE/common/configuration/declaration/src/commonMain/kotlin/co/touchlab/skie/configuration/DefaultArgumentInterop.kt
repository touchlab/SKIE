package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

object DefaultArgumentInterop {

    object Enabled : ConfigurationKey.Boolean, ConfigurationScope.All {

        override val defaultValue: Boolean = false

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<DefaultArgumentInterop.Enabled>() -> true
            configurationTarget.hasAnnotation<DefaultArgumentInterop.Disabled>() -> false
            else -> null
        }
    }

    object MaximumDefaultArgumentCount : ConfigurationKey.Int, ConfigurationScope.All {

        override val defaultValue: Int = 5

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Int? =
            configurationTarget.findAnnotation<DefaultArgumentInterop.MaximumDefaultArgumentCount>()?.count
    }
}

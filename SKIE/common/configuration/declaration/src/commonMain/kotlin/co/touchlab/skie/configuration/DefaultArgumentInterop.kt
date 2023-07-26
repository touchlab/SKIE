package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

object DefaultArgumentInterop {

    object Enabled : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = true

        override val skieRuntimeValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Enabled>() -> true
                configurationTarget.hasAnnotation<DefaultArgumentInterop.Disabled>() -> false
                else -> null
            }
    }

    object MaximumDefaultArgumentCount : ConfigurationKey.Int {

        override val defaultValue: Int = 5

        override val skieRuntimeValue: Int = 5

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Int? =
            configurationTarget.findAnnotation<DefaultArgumentInterop.MaximumDefaultArgumentCount>()?.count
    }
}

package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.SuspendInterop

object SuspendInterop {

    /**
     * If true, the interop code is generated for the given suspend function.
     */
    object Enabled : ConfigurationKey.Boolean, ConfigurationScope.AllExceptConstructorsAndProperties {

        override val defaultValue: Boolean = true

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? = when {
            configurationTarget.hasAnnotation<SuspendInterop.Enabled>() -> true
            configurationTarget.hasAnnotation<SuspendInterop.Disabled>() -> false
            else -> null
        }
    }
}

package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.FunctionInterop

object FunctionInterop {

    object FileScopeConversion {

        /**
         * If true, SKIE generates wrappers for global functions and interface extensions which allows them to be called with conventional syntax.
         * Original functions are still available under their original scope (class named after the file in which they are declared).
         */
        object Enabled : ConfigurationKey.Boolean {

            override val defaultValue: Boolean = true

            override val skieRuntimeValue: Boolean = false

            override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
                when {
                    configurationTarget.hasAnnotation<FunctionInterop.FileScopeConversion.Enabled>() -> true
                    configurationTarget.hasAnnotation<FunctionInterop.FileScopeConversion.Disabled>() -> false
                    else -> null
                }
        }
    }
}

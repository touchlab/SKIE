package co.touchlab.skie.configuration

import co.touchlab.skie.configuration.annotations.ClassInterop

object ClassInterop {

    /**
     * If enabled SKIE generates type aliases with relatively stable names to the `Skie` namespace.
     * These type aliases can be used in handwritten Swift code that is bundled by SKIE into the generated framework.
     * This solves the problem of not knowing upfront what names will be given to Kotlin classes by the compiler.
     *
     * (Both Kotlin compiler and SKIE rename classes to avoid name conflicts.)
     */
    object StableTypeAliases : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override val skieRuntimeValue: Boolean = true

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<ClassInterop.StableTypeAliases.Enabled>() -> true
                configurationTarget.hasAnnotation<ClassInterop.StableTypeAliases.Disabled>() -> false
                else -> null
            }
    }
}

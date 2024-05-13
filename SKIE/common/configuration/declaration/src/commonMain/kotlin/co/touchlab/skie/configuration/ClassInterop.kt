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
    object StableTypeAlias : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = false

        override val skieRuntimeValue: Boolean = true

        override fun findAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<ClassInterop.StableTypeAlias.Enabled>() -> true
                configurationTarget.hasAnnotation<ClassInterop.StableTypeAlias.Disabled>() -> false
                else -> null
            }
    }

    /**
     * Specifies the name of the Swift framework which contains this external Obj-C class.
     * External Obj-C classes provided by Kotlin (from the `platform` package) are configured automatically.
     * However, external Obj-C classes with custom bindings (for example generated by the cinterop tool) must be configured manually.
     * Without knowing the Framework name, SKIE cannot reference such a class in the generated code.
     */
    object CInteropFrameworkName : ConfigurationKey.OptionalString, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: String? = null
    }

    /**
     * If enabled, SKIE automatically configures CInteropFrameworkName for external Obj-C classes originating from Cocoapods.
     * The framework name can be overridden by using the CInteropFrameworkName configuration.
     */
    object DeriveCInteropFrameworkNameFromCocoapods : ConfigurationKey.Boolean, ConfigurationScope.AllExceptCallableDeclarations {

        override val defaultValue: Boolean = true
    }
}

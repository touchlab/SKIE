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

    /**
     * If enabled, SKIE uses the original Kotlin compiler algorithm for naming functions and properties.
     *
     * The original algorithm mangles conflicting names by adding underscore to the last argument label or the function identifier if there are no value parameters.
     * The conflict resolution algorithm works well for Obj-C code but is way too conservative for Swift.
     * Namely because Swift allows overloading functions with different value parameters types whereas Obj-C overloading considers only argument labels.
     * For example:
     *
     * ```kotlin
     * fun foo(a: Int) {}
     *
     * fun foo(a: String) {}
     * ```
     *
     * is translated to:
     *
     * ```swift
     * func foo(a: Int32) {}
     *
     * func foo(a_: String) {}
     * ```
     *
     * SKIE uses a different naming algorithm that always adds the underscore to the function identifier which adds consistency to the generated code.
     * Additionally, it uses a custom conflict resolution algorithm that better matches Swift overloading rules.
     *
     * For the above example, SKIE generates:
     * ```swift
     * func foo(a: Int32) {}
     *
     * func foo(a: String) {}
     * ```
     *
     * Conflicts can still occur, but they are much less frequent.
     * Most common example is when the value parameter type is a Kotlin value class which is inlined by the compiler.
     * For example:
     *
     * ```kotlin
     * value class V(val value: Int)
     *
     * fun foo(a: Int) {}
     *
     * fun foo(a: V) {}
     * ```
     *
     * In this case SKIE generates:
     *
     * ```swift
     * func foo(a: Int32) {}
     *
     * func foo_(a: Int32) {}
     * ```
     *
     * Note:
     * This option does not influence how SKIE treats conflicts between a function without value parameters and a property.
     * These constructs do not conflict in Obj-C, but they do in Swift.
     * Therefore, SKIE has to resolve the conflict even though Kotlin compiler doesn't have to.
     */
    object LegacyName : ConfigurationKey.Boolean {

        override val defaultValue: Boolean = false

        override val skieRuntimeValue: Boolean = false

        override fun getAnnotationValue(configurationTarget: ConfigurationTarget): Boolean? =
            when {
                configurationTarget.hasAnnotation<FunctionInterop.LegacyName.Enabled>() -> true
                configurationTarget.hasAnnotation<FunctionInterop.LegacyName.Disabled>() -> false
                else -> null
            }
    }
}

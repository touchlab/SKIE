---
title: Available Annotations
---

```kotlin
object DataStruct {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}

object DefaultArgumentInterop {

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
    @Retention(AnnotationRetention.BINARY)
    annotation class MaximumDefaultArgumentCount(val count: Int)
}

object EnumInterop {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}

object ExperimentalFeatures {

    /**
     * Enables experimental features for the annotated declaration.
     * Experimental features might not be fully implemented yet which may cause compilation problems.
     * Has effect only if the experimental features are globally disabled.
     */
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    /**
     * Disables experimental features for the annotated declaration.
     */
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}


object SealedInterop {

    /**
     * Enables the sealed interop for the annotated declaration.
     * Has effect only if the interop is globally disabled.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    /**
     * Disables the sealed interop for the annotated declaration.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled

    object Function {

        /**
         * Changes the name for the generated function used inside `switch`.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Name(val name: String)

        /**
         * Changes the argument label for the generated function used inside `switch`.
         * Disable the argument label by passing "_".
         * No argumentLabel is generated if the name is empty.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class ArgumentLabel(val argumentLabel: String)

        /**
         * Changes the parameter name for the generated function used inside `switch`.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class ParameterName(val parameterName: String)
    }

    /**
     * Changes the name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class ElseName(val elseName: String)

    object Case {

        /**
         * Hides this subclass from the generated code, which means no dedicated enum case will be generated for it.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Hidden

        /**
         * The dedicated enum case will be generated for this subclass even if the global configuration is set to hidden.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Visible

        /**
         * Changes the name of the enum case generated for this subclass.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.BINARY)
        annotation class Name(val name: String)
    }
}
```

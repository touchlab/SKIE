package co.touchlab.swiftgen.api

object SealedInterop {

    /**
     * Enables the sealed interop for the annotated declaration.
     * Has effect only if the interop is globally disabled.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Enabled

    /**
     * Disables the sealed interop for the annotated declaration.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Disabled

    /**
     * Changes the name for the generated function used inside `switch`.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class FunctionName(val functionName: String)

    /**
     * Changes the name for the custom `else` case that is generated if some children are hidden / not accessible from Swift.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ElseName(val elseName: String)

    object Case {

        /**
         * Hides this subclass from the generated code, which means no dedicated enum case will be generated for it.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Hidden

        /**
         * The dedicated enum case will be generated for this subclass even if the global configuration is set to hidden.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Visible

        /**
         * Changes the name of the enum case generated for this subclass.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Name(val name: String)
    }
}

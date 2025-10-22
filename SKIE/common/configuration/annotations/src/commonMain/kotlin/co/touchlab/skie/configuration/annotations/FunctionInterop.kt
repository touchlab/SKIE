package co.touchlab.skie.configuration.annotations

@Target
annotation class FunctionInterop {

    @Target
    annotation class FileScopeConversion {

        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
        @Retention(AnnotationRetention.BINARY)
        annotation class Enabled

        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
        @Retention(AnnotationRetention.BINARY)
        annotation class Disabled
    }

    /**
     * See [co.touchlab.skie.configuration.FunctionInterop.LegacyName]
     */
    @Target
    annotation class LegacyName {

        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
        @Retention(AnnotationRetention.BINARY)
        annotation class Enabled

        @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
        @Retention(AnnotationRetention.BINARY)
        annotation class Disabled
    }
}

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
}

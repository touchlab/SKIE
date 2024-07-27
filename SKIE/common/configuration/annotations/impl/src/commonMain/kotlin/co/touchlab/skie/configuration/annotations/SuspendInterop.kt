package co.touchlab.skie.configuration.annotations

@Target
annotation class SuspendInterop {

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}

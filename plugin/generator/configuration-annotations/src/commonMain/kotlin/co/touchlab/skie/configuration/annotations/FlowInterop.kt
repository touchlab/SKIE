package co.touchlab.skie.configuration.annotations

@Target
annotation class FlowInterop {

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}

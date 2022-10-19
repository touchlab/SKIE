package co.touchlab.swiftgen.api

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

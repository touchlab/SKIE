package co.touchlab.skie.configuration.annotations

object DataStruct {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Enabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Disabled
}

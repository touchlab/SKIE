package co.touchlab.swiftgen.api

object SwiftSealedCase {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Hidden

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Visible

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Name(val name: String)
}

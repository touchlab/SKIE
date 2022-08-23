package co.touchlab.swiftgen.api

object SealedInterop {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Enabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Disabled

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class FunctionName(val functionName: String)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ElseName(val elseName: String)

    object Case {

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
}

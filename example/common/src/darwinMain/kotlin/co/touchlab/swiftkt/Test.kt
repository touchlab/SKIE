package co.touchlab.swiftkt

@Retention(AnnotationRetention.RUNTIME)
annotation class Test(
    val rename: String = "",
    val hide: Boolean = false,
    val remove: Boolean = false,
    val invocation: String = "",
)

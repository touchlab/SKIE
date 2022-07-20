package co.touchlab.swiftkt

@ThisTest
class KotlinTest {
    val hello = "World"
}

@Retention(AnnotationRetention.RUNTIME)
annotation class ThisTest

package co.touchlab.swikt

@ThisTest
class KotlinTest {
    val hello = "World"
}

@Retention(AnnotationRetention.RUNTIME)
annotation class ThisTest

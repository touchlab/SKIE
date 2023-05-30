package tests.bugs.value_property_collision

open class Foo

private interface A {
    val value: Int
}

enum class B: A {
    B1;

    override val value: Int = 0
}

val Foo.value: String
    get() = throw UnsupportedOperationException()

package `tests`.`enums`.`with_members`.`properties`

enum class A {
    A1,
    A2;

    val foo: Int = 0

    var bar: String = "Hello world"
}

fun randomA(): A {
    return A.values().random()
}
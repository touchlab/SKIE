package `tests`.`enums`.`basic`.`single_case`

enum class A {
    A1
}

fun randomA(): A {
    return A.values().random()
}
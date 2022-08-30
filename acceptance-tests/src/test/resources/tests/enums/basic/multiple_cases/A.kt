package tests.enums.basic.multiple_cases

enum class A {
    A1,
    A2,
    A3
}

fun randomA(): A {
    return A.values().random()
}

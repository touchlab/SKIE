package `tests`.`enums`.`interactions`.`with_sealed_interface`

enum class A : I {
    A1, A2
}

sealed interface I

fun getI(): I = A.A1

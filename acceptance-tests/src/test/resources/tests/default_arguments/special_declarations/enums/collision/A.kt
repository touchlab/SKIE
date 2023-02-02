package `tests`.`default_arguments`.`special_declarations`.`enums`.`collision`

enum class A {

    X;

    fun foo(i: Int = 0, k: Int = 1): Int = i - k

    fun foo(i: Int): Int = i + 0
}

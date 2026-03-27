package `tests`.`default_arguments`.`special_declarations`.`enums`.`static`

enum class A {

    X;

    fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int = i + k * m - o
}

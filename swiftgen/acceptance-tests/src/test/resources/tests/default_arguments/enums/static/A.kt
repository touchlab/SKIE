package `tests`.`default_arguments`.`enums`.`static`

enum class A {

    X;

    fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int = i + k * m - o
}

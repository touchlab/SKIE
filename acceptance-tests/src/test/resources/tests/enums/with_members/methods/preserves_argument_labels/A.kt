package `tests`.`enums`.`with_members`.`methods`.`preserves_argument_labels`

enum class A {
    A1;

    fun foo(i: Int): Int = i

    fun foo(i: String): String = i
}

package `tests`.`default_arguments`.`objects`.`methods`

object A {

    fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int = i + k * m - o
}

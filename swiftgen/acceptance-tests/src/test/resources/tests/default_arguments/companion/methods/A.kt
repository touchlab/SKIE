package `tests`.`default_arguments`.`companion`.`methods`

class A {

    companion object {

        fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int = i + k * m - o
    }
}

package `tests`.`default_arguments`.`classes`.`methods`.`virtual`.`abstract`

abstract class A {

    abstract fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int
}

class B : A() {

    override fun foo(i: Int, k: Int, m: Int, o: Int): Int = i + k * m - o
}

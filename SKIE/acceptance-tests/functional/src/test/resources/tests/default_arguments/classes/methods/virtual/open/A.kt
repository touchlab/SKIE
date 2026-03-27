package `tests`.`default_arguments`.`classes`.`methods`.`virtual`.`open`

open class A {

    open fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int = 0
}

class B : A() {

    override fun foo(i: Int, k: Int, m: Int, o: Int): Int = i + k * m - o
}

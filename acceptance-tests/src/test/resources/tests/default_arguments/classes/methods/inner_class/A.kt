package `tests`.`default_arguments`.`classes`.`methods`.`inner_class`

class A {

    val b = B()

    inner class B {

        fun foo(i: Int = 0): Int = i
    }
}
package `tests`.`default_arguments`.`classes`.`methods`.`visibility`.`nested_class`.`internal_nested`

class A {

    internal class B {

        fun foo(i: Int = 0): Int = i
    }
}
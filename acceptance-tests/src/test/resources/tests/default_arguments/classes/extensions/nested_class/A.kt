package `tests`.`default_arguments`.`classes`.`extensions`.`nested_class`

class A {

    class B {

        class C {

            val zero = 0
        }
    }
}

fun A.B.C.foo(i: Int = zero): Int = i

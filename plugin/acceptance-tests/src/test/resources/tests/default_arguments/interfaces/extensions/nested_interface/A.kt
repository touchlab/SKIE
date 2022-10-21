package `tests`.`default_arguments`.`interfaces`.`extensions`.`nested_interface`

interface A {

    interface B {

        interface C {

            val zero: Int
        }

        class CImpl : C {

            override val zero: Int = 0
        }
    }
}

fun A.B.C.foo(i: Int = zero): Int = i

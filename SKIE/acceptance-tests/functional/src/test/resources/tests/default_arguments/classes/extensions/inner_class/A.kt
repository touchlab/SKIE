package `tests`.`default_arguments`.`classes`.`extensions`.`inner_class`

class A {

    val b = B()

    inner class B {

        val c = C()

        inner class C {

            val zero = 0
        }
    }
}

fun A.B.C.foo(i: Int = zero): Int = i

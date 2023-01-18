package `tests`.`enums`.`nested`.`enum_in_class_in_class`

class A {
    class B {
        enum class C {
            C1,
            C2
        }
    }
}

fun c1(): A.B.C = A.B.C.C1
fun c2(): A.B.C = A.B.C.C2

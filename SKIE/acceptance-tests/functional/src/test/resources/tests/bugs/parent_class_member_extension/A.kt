package `tests`.`bugs`.`parent_class_member_extension`

open class A {

    val Int.foo: Double
        get() = 0.0
}

class B : A()

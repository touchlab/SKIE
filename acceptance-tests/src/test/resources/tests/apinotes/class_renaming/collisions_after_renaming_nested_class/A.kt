package tests.apinotes.class_renaming.collisions_after_renaming_nested_class

@JvmInline
value class V(val value: Int) {

    class A(val value: V)
}

class VA

fun getA(value: V): V.A = V.A(value)

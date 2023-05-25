package tests.bugs.nested_enum_as_generic_type_parameter

abstract class A<FOO>

class B: A<B.C>() {

    enum class C {
        C1
    }
}

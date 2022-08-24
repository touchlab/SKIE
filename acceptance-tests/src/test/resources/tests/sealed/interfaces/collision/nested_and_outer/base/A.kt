package tests.sealed.interfaces.collision.nested_and_outer.base

sealed interface A {

    class A1 : A
    class A2 : A
}

class Wrapper {

    sealed interface A {

        class A1 : A
        class A2 : A
    }
}

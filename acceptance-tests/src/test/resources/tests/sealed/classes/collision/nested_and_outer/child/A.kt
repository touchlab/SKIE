package tests.sealed.classes.collision.nested_and_outer.child

sealed class A {

    class A1 : A()
}

class A1 : A()

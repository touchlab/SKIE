package tests.sealed.interfaces.collission.nested_and_outer

sealed interface A {

    class A1 : A
}

class A1 : A

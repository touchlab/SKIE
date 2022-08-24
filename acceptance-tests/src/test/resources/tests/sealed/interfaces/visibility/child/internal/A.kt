package tests.sealed.interfaces.visibility.child.internal

sealed interface A {

    companion object {

        fun createA1(): A = A1()
    }
}

internal class A1 : A

class A2(val k: Int) : A

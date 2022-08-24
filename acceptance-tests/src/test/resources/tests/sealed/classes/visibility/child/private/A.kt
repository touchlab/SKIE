package tests.sealed.classes.visibility.child.private

sealed class A {

    private class A1 : A()

    class A2(val k: Int) : A()

    companion object {

        fun createA1(): A = A1()
    }
}


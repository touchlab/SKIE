package tests.sealed.classes.no_accessible_children.visibility

sealed class A {

    private class A1 : A()

    private class A2 : A()

    companion object {

        fun createA1(): A = A1()
    }
}


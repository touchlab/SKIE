package tests.sealed.classes.visibility.child.protected

sealed class A {

    protected class A1 : A()

    class A2 : A()
}


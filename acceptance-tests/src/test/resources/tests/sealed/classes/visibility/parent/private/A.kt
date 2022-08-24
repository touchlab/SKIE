package tests.sealed.classes.visibility.parent.private

private sealed class A {

    class A1 : A()
    class A2 : A()
}

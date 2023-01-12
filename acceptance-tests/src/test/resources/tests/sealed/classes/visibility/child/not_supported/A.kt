package tests.sealed.classes.visibility.child.not_supported

sealed class A {

    class A1(delegate: List<Int>) : A(), List<Int> by delegate

    class A2(val k: Int) : A()

    companion object {

        fun createA1(): A = A1(emptyList())
    }
}

package tests.bugs.sealed_child_inner_in_generic_parent

sealed class A<T> {

}

class Parent<T> {

    inner class A1: A<T>()
}

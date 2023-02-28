package tests.bugs.generic_fake_objc_constructor

abstract class A<T>(i: T)

object B : A<Int>(0)

package tests.sealed.interfaces.objects

sealed interface A

class A1(val value_A1: Int) : A
object A2 : A {

    val value_A2: Int = 0
}
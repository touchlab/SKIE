package tests.coroutines.suspend.interfaces.extensions.generics.function.receiver

interface A

class A1 : A

suspend fun <S: A> S.foo(): Int = 0

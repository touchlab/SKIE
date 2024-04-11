package templates.basic

enum class BasicEnum {
    A,
    B,
    C,
}

class ClassWithSuspend {
    suspend fun noArgNoReturn() {}
}

interface InterfaceWithSuspend {
    suspend fun noArgNoReturn()
}

sealed interface SealedInterface {
    object A: SealedInterface
    class B: SealedInterface
}

sealed class SealedClass {
    object A: SealedClass()
    class B: SealedClass()
}

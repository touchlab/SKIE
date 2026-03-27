package `tests`.`bugs`.`return_type_collision_detection`

open class A

class B : A()

class C

class T<T>

value class I(val value: Int)

enum class Required {
    A1;

    fun renamed(i: Int): A = A()

    fun renamed(i: I): B = B()

    fun notRenamed(i: Int): A = A()

    fun notRenamed(i: I): C = C()
}

enum class Optional {
    A1;

    fun renamed(i: Int): A? = null

    fun renamed(i: I): B? = null

    fun notRenamed(i: Int): A? = null

    fun notRenamed(i: I): B = B()
}

enum class Generics {
    A1;

    fun notRenamed(i: Int): T<A> = T()

    fun notRenamed(i: I): T<B> = T()
}

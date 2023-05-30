package tests.bugs.override_return_type

interface A<T> {
    fun foo(): List<T>
}

class B: A<B> {
    override fun foo(): MutableList<B> = throw UnsupportedOperationException()
}

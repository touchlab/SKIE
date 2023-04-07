package tests.bugs.function_named_deinit;

enum class A {
    Q;

    fun deinit(i: Int) = i
}

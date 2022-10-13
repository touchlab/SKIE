package tests.default_arguments.enums.virtual

enum class A {

    X {
        override fun foo(i: Int, k: Int, m: Int, o: Int): Int = i + k + m + o
    };

    abstract fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int
}

package `tests`.`default_arguments`.`interfaces`.`method`.`delegated`

interface A {

    fun foo(i: Int, k: Int = 1, m: Int, o: Int = 3): Int
}

class B(delegate: A) : A by delegate

class C : A {

    override fun foo(i: Int, k: Int, m: Int, o: Int): Int = 0
}

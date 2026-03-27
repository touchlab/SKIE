package `tests`.`bugs`.`generic_function_inherited_from_two_interfaces_is_duplicated_by_compiler`

interface I1<T> {

    fun get(value: T): T
}

interface I2<T> {

    fun get(value: T): T
}

open class A : I1<Int>, I2<Int> {

    override fun get(value: Int): Int = value
}

class B : A() {

    override fun get(value: Int): Int = value
}

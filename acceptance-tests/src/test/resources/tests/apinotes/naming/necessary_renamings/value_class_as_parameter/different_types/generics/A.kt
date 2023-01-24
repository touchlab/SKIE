package `tests`.`apinotes`.`naming`.`necessary_renamings`.`value_class_as_parameter`.`different_types`.`generics`

@JvmInline
value class V(val value: Int)

class A<T, U> {

    fun foo(a: T, i: V) = i.value + 1

    fun foo(a: U, i: Int) = i
}

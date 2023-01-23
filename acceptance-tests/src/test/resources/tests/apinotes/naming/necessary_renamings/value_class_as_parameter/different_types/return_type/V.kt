package `tests`.`apinotes`.`naming`.`necessary_renamings`.`value_class_as_parameter`.`different_types`.`return_type`

@JvmInline
value class V(val value: Int)

fun foo(i: V): String = i.toString()

fun foo(i: Int) = i

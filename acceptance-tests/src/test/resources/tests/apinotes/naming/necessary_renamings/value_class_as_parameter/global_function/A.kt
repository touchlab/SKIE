package `tests`.`apinotes`.`naming`.`necessary_renamings`.`value_class_as_parameter`.`global_function`

@JvmInline
value class V(val value: Int)

fun foo(i: V) = i.value + 1

fun foo(i: Int) = i

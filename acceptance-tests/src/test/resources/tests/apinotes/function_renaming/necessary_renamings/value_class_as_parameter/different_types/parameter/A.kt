package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`value_class_as_parameter`.`different_types`.`parameter`

@JvmInline
value class V(val value: Int)

fun foo(a: String, i: V) = i.value + 1

fun foo(a: Int, i: Int) = i

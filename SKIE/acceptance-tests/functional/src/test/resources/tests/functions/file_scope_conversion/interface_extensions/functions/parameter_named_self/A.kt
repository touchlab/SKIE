package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`parameter_named_self`

import tests.functions.file_scope_conversion.interface_extensions.I

fun I.foo(i: Int, self: Int): Int = i - self - value

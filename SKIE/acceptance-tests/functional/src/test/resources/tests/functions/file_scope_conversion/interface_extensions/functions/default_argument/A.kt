package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`default_argument`

import tests.functions.file_scope_conversion.interface_extensions.I

fun I.foo(i: Int = 1, k: Int = 2): Int = i - k - value

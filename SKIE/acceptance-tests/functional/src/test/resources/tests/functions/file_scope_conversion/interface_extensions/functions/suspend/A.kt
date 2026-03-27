package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`suspend`

import tests.functions.file_scope_conversion.interface_extensions.I

suspend fun I.foo(i: Int, k: Int): Int = i - k - value

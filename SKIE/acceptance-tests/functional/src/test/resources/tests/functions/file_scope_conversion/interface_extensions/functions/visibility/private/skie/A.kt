package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`visibility`.`private`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility
import tests.functions.file_scope_conversion.interface_extensions.I

@SkieVisibility.Private
fun I.foo(i: Int, k: Int): Int = i - k - value

package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`functions`.`visibility`.`private`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
fun foo(i: Int, k: Int): Int = i - k

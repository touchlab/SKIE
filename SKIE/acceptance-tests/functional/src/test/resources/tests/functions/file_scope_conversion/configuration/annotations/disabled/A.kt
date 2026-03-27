package `tests`.`functions`.`file_scope_conversion`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.annotations.FunctionInterop

@FunctionInterop.FileScopeConversion.Disabled
fun foo(i: Int, k: Int): Int = i - k

package `tests`.`functions`.`file_scope_conversion`.`configuration`.`annotations`.`enabled`

import co.touchlab.skie.configuration.annotations.FunctionInterop

@FunctionInterop.FileScopeConversion.Enabled
fun foo(i: Int, k: Int): Int = i - k

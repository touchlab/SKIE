package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`functions`.`suspend`.`without_interop`

import co.touchlab.skie.configuration.annotations.SuspendInterop

@SuspendInterop.Disabled
suspend fun foo(i: Int, k: Int): Int = i - k

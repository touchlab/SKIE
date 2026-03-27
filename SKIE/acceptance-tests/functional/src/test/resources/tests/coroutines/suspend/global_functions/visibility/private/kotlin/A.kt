package `tests`.`coroutines`.`suspend`.`global_functions`.`visibility`.`private`.`kotlin`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
suspend fun foo(): Int = 0

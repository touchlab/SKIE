package `tests`.`coroutines`.`flow`.`visibility`.`private`

import co.touchlab.skie.configuration.annotations.SkieVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@SkieVisibility.Private
fun foo(): Flow<Int> = flowOf(1, 2, 3)

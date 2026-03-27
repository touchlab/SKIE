package `tests`.`coroutines`.`flow`.`visibility`.`internal`

import co.touchlab.skie.configuration.annotations.SkieVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@SkieVisibility.Internal
fun foo(): Flow<Int> = flowOf(1, 2, 3)

package `tests`.`coroutines`.`flow`.`mapping`.`property`.`global_functions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

var foo: Flow<Int> = flowOf(1, 2, 3)
val foo2: Flow<Int> = flowOf(1, 2, 3).map { it * 2 }

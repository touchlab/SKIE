package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`visibility`.`private`.`class`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

suspend fun A.foo(): Int = 0

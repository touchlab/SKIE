package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`visibility`.`private`.`function`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A

@SkieVisibility.Private
suspend fun A.foo(): Int = 0

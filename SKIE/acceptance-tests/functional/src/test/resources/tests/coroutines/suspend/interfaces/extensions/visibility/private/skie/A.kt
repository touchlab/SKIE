package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`visibility`.`private`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface A

class A1 : A

suspend fun A.foo(): Int = 0

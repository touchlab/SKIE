package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`visibility`.`internal`.`class`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A

@SkieVisibility.Internal
suspend fun A.foo(): Int = 0

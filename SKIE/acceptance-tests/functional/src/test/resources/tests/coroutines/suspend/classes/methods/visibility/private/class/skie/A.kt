package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`visibility`.`private`.`class`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {

    suspend fun foo(): Int = 0
}

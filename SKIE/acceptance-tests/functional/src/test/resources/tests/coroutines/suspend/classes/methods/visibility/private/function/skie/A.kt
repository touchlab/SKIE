package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`visibility`.`private`.`function`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A {

    @SkieVisibility.Private
    suspend fun foo(): Int = 0
}

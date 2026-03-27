package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`visibility`.`private`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface A {

    suspend fun foo(): Int = 0
}

class A1 : A

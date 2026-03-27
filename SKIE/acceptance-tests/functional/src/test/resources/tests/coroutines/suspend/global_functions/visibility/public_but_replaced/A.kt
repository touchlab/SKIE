package tests.coroutines.suspend.global_functions.visibility.public_but_replaced

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.PublicButReplaced
suspend fun foo(): Int = 0

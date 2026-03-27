package tests.visibility.annotation.internal_if_wrapped.suspend_method.final

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A {

    @SkieVisibility.InternalIfWrapped
    suspend fun foo() {
    }
}

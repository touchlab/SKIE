package tests.visibility.annotation.validation.abstract_member.`interface`.explicit

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
abstract class I {

    @SkieVisibility.Internal
    abstract fun foo(i: Int)
}

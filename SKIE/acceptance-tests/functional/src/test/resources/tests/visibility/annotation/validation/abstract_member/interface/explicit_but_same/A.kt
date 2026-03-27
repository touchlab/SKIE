package tests.visibility.annotation.validation.abstract_member.`interface`.explicit

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface I {

    @SkieVisibility.Internal
    fun foo(i: Int)
}

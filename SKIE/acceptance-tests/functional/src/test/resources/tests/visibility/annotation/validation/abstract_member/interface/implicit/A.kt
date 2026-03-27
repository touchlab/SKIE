package tests.visibility.annotation.validation.abstract_member.`interface`.implicit

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface I {

    fun foo(i: Int)
}

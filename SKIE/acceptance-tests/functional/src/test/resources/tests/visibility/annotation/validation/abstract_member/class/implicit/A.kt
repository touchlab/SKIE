package tests.visibility.annotation.validation.abstract_member.`interface`.implicit

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
abstract class A {

    abstract fun foo(i: Int)
}

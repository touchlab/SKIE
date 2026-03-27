package tests.visibility.annotation.public_but_replaced.method.parent

import co.touchlab.skie.configuration.annotations.SkieVisibility

open class A {

    @SkieVisibility.PublicButReplaced
    open fun foo() {
    }
}

class B : A() {

    override fun foo() {
    }
}

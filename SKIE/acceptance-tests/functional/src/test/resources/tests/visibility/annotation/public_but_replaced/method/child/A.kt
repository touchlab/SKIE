package tests.visibility.annotation.public_but_replaced.method.child

import co.touchlab.skie.configuration.annotations.SkieVisibility

open class A {

    open fun foo() {
    }
}

class B : A() {

    @SkieVisibility.PublicButReplaced
    override fun foo() {
    }
}

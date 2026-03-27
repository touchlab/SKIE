package tests.visibility.annotation.public_but_replaced.method.complex

import co.touchlab.skie.configuration.annotations.SkieVisibility

open class A {

    @SkieVisibility.PublicButReplaced
    open fun foo() {
    }
}

interface I {

    fun foo()
}

class B : A(), I {

    override fun foo() {
    }
}

class C : I {

    override fun foo() {
    }
}

package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`complex`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface I

interface K : I {

    abstract class X
}

@SkieVisibility.Private
class Y : K.X()

abstract class A<T : K.X>

abstract class B : A<Y>()

class C : B()

fun foo(): C = C()

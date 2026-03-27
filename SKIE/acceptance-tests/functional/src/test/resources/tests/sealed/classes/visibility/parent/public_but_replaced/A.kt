package tests.sealed.classes.visibility.child.public_but_replaced

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.PublicButReplaced
sealed class A {

    class A1 : A()
    class A2 : A()
}


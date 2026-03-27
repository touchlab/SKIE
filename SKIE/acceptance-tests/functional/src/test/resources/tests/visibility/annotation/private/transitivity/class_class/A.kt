package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {

    @SkieVisibility.Internal
    class B {
    }
}

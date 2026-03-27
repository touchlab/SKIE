package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`interface_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface A {

    @SkieVisibility.Internal
    class B {
    }
}

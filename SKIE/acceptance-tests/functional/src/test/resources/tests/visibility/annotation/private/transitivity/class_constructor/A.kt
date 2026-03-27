package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_constructor`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {

    @SkieVisibility.Internal
    constructor() {
    }
}

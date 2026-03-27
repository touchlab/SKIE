package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_member_property`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {

    @SkieVisibility.Internal
    val foo: Int = 0
}

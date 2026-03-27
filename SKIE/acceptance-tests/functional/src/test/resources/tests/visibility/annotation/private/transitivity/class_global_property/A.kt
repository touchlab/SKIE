package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_global_property`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {
}

@SkieVisibility.Internal
val foo: A = A()

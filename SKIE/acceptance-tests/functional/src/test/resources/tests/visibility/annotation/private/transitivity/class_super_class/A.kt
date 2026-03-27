package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_super_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
open class A

@SkieVisibility.Internal
class B : A()

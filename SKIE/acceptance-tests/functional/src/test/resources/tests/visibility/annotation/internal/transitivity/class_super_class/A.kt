package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_super_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
open class A

class B : A()

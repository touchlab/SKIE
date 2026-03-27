package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_super_interface`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface I

class A : I

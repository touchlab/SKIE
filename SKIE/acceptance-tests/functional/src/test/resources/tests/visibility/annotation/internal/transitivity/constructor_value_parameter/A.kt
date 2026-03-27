package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`constructor_value_parameter`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A

class B(val a: A)

package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`constructor_value_parameter`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

@SkieVisibility.Internal
class B(val a: A)

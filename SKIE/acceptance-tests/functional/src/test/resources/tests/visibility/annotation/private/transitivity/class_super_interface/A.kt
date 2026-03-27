package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_super_interface`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface I

class A : I

package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`interface_super_interface`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface A

interface B : A

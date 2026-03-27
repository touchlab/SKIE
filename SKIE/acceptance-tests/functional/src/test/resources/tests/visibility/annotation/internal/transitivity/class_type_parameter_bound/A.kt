package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_type_parameter_bound`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
abstract class I

class B<T : I>

fun foo(): B<*> = B<I>()

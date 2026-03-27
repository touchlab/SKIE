package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_type_parameter_bound`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
abstract class I

class B<T : I>

fun foo(): B<*> = B<I>()

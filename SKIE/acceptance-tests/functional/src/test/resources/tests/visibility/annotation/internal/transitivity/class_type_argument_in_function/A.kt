package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_type_argument_in_function`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A

class B<T>

fun foo(): B<A> = B()

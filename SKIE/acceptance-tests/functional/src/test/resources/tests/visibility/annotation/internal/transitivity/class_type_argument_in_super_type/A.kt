package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_type_argument_in_super_type`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A

abstract class B<T>

class C : B<A>()

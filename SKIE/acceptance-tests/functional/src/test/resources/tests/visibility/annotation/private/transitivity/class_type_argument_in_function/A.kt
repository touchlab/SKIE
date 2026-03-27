package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_type_argument_in_function`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

class B<T>

@SkieVisibility.Internal
fun foo(): B<A> = B()

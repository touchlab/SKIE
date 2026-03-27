package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_type_argument_in_super_type`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

abstract class B<T>

@SkieVisibility.Internal
class C : B<A>()

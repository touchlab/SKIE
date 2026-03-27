package `tests`.`visibility`.`annotation`.`internal`.`file_class`.`some_children_not_accessible`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

fun foo() {
}

@SkieVisibility.Internal
fun I.foo() {
}

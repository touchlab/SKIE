package `tests`.`visibility`.`annotation`.`internal`.`file_class`.`all_children_not_accessible`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

@SkieVisibility.Internal
fun foo() {
}

@SkieVisibility.Internal
fun I.foo() {
}

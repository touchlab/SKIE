package `tests`.`visibility`.`annotation`.`private`.`file_class`.`some_children_not_accessible`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

fun foo() {
}

@SkieVisibility.Private
fun I.foo() {
}

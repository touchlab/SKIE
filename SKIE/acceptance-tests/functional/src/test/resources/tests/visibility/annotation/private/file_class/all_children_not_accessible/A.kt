package `tests`.`visibility`.`annotation`.`private`.`file_class`.`all_children_not_accessible`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

@SkieVisibility.Private
fun foo() {
}

@SkieVisibility.Private
fun I.foo() {
}

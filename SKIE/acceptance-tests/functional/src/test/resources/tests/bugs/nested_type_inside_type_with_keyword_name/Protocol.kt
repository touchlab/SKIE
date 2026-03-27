package `tests`.`bugs`.`nested_type_inside_type_with_keyword_name`

import co.touchlab.skie.configuration.annotations.ClassInterop

class Protocol {

    companion object {

        fun foo() {
        }
    }

    @ClassInterop.StableTypeAlias.Enabled
    class A {
    }
}


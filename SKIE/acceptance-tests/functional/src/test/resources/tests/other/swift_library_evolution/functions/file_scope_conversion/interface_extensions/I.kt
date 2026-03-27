package `tests`.`other`.`swift_library_evolution`.`functions`.`file_scope_conversion`.`interface_extensions`

interface I {

    val value: Int
}

class C(override val value: Int) : I

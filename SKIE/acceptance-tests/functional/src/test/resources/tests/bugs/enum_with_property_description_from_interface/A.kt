package `tests`.`bugs`.`enum_with_property_description_from_interface`

interface I {

    val description: String
}

enum class A : I {
    q;

    override val description: String = "A"
}

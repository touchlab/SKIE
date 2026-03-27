package `tests`.`enums`.`with_members`.`overriden_immutable_and_later_mutable_property`

interface I {

    val foo: Int
}

interface MutableI : I {

    override var foo: Int
}

enum class A : MutableI {
    A1;

    override var foo: Int = 1
}

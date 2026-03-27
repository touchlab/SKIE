package `tests`.`bugs`.`hash_property_with_hash_code`

class A {

    val hash: Int = 0

    override fun hashCode(): Int = 1

    override fun equals(other: Any?): Boolean = false
}

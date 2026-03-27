package `tests`.`default_arguments`.`classes`.`constructors`.`collision_detection`.`collision`.`constructor`

class A(i: Int = 0, k: Int = 1) {

    val value = i - k

    constructor(i: Int = 0) : this(i, i)
}

package `tests`.`default_arguments`.`classes`.`constructors`.`collision_detection`.`no_collision`.`different_parameter_type`

class A(i: Int = 0, k: Int = 1) {

    val value = i - k

    constructor(i: String) : this(0, 0)
}

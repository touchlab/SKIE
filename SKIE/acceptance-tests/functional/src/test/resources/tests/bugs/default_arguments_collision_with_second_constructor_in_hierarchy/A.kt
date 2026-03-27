package `tests`.`bugs`.`default_arguments_collision_with_second_constructor_in_hierarchy`

abstract class A(
    i: Int,
    k: Int = 0,
) {

    constructor(i: Int) : this(i = i, k = 1)
}

class B : A(0)

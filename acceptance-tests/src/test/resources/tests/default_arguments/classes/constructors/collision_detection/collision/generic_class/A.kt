package `tests`.`default_arguments`.`classes`.`constructors`.`collision_detection`.`collision`.`generic_class`

class A<T: Int> {

    val value: Int

    constructor(i: T) {
        value = i
    }

    constructor(i: Int = 0, k: Int = 1) {
        value = i - k
    }
}

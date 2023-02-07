package `tests`.`default_arguments`.`classes`.`constructors`.`inheritance`

open class A(i: Int, k: Int = 1) {

    val value: Int = i - k
}

class B(i: Int): A(i + 1)

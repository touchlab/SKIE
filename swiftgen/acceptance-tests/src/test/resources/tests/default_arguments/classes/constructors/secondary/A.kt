package `tests`.`default_arguments`.`classes`.`constructors`.`secondary`

class A(val value: Int) {

    constructor(i: Int, k: Int = 1, m: Int, o: Int = 3) : this(i + k * m - o)
}

package `tests`.`default_arguments`.`classes`.`constructors`.`primary`

class A(i: Int, k: Int = 1, m: Int, o: Int = 3) {

    val value: Int = i + k * m - o
}

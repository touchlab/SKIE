package `tests`.`default_arguments`.`classes`.`constructors`.`inner`.`primary`

class A {

    inner class B {

        inner class C(i: Int, k: Int = 1, m: Int, o: Int = 3) {

            val value: Int = i + k * m - o
        }
    }
}

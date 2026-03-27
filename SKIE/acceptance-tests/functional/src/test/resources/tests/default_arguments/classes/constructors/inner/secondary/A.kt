package `tests`.`default_arguments`.`classes`.`constructors`.`inner`.`secondary`

class A {

    inner class B {

        inner class C(val value: Int) {

            constructor(i: Int, k: Int = 1, m: Int, o: Int = 3) : this(i + k * m - o)
        }
    }
}

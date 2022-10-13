package `tests`.`sealed`.`interfaces`.`visibility`.`child`.`private`

sealed interface A {

    private class A1 : A

    class A2(val k: Int) : A

    companion object {

        fun createA1(): A = A1()
    }
}

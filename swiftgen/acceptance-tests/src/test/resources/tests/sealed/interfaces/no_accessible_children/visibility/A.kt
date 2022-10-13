package `tests`.`sealed`.`interfaces`.`no_accessible_children`.`visibility`

sealed interface A {

    private class A1 : A

    private class A2 : A

    companion object {

        fun createA1(): A = A1()
    }
}

package `tests`.`sealed`.`interfaces`.`visibility`.`child`.`protected`

class Wrapper {

    sealed interface A

    protected class A1 : A

    class A2 : A
}
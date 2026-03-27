package `tests`.`sealed`.`interfaces`.`visibility`.`parent`.`protected`

abstract class Wrapper {

    protected sealed interface A {

        class A1 : A
        class A2 : A
    }
}

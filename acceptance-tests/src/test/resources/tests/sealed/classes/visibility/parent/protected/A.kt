package `tests`.`sealed`.`classes`.`visibility`.`parent`.`protected`

abstract class Wrapper {

    protected sealed class A {

        class A1 : A()
        class A2 : A()
    }
}
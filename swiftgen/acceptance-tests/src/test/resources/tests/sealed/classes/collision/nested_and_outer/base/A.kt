package `tests`.`sealed`.`classes`.`collision`.`nested_and_outer`.`base`

sealed class A {

    class A1 : A()
    class A2 : A()
}

class Wrapper {

    sealed class A {

        class A1 : A()
        class A2 : A()
    }
}

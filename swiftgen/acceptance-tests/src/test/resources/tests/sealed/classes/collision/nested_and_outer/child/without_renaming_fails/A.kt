package `tests`.`sealed`.`classes`.`collision`.`nested_and_outer`.`child`.`without_renaming_fails`

sealed class A {

    class A1 : A()
}

class A1 : A()

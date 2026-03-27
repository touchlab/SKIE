package `tests`.`sealed`.`interfaces`.`collision`.`nested_and_outer`.`child`.`without_renaming_fails`

sealed interface A {

    class A1 : A

    class AA1 : A
}

class A1 : A

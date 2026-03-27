package `tests`.`sealed`.`classes`.`collision`.`nested_and_outer`.`child`.`without_renaming_is_renamed_automatically`

sealed class A {

    class A1 : A()

    class AA1 : A()
}

class A1 : A()

package `tests`.`apinotes`.`naming`.`unnecessary_renamings_are_fixed`.`inheritance`

interface A {

    fun foo(i: Int) = i
}

class A1 : A {

    fun foo(i: String) = i.toInt()
}

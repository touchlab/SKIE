package `tests`.`default_arguments`.`classes`.`methods`.`throws`

class A {

    @Throws(IllegalArgumentException::class)
    fun foo(i: Int = 0): Int = i
}
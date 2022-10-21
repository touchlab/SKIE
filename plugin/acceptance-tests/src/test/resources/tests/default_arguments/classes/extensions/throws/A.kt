package `tests`.`default_arguments`.`classes`.`extensions`.`throws`

class A

@Throws(IllegalArgumentException::class)
fun A.foo(i: Int = 0): Int = i

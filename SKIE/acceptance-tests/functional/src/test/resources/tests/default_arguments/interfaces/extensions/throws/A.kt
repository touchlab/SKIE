package `tests`.`default_arguments`.`interfaces`.`extensions`.`throws`

interface A

@Throws(IllegalArgumentException::class)
fun A.foo(i: Int = 0): Int = i

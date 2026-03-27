package `tests`.`default_arguments`.`global_functions`.`multiple_functions`

fun bar(i: Int, k: Int = 1): Int = i + k

fun foo(i: Int = 0, k: Int): Int = i + k

package `tests`.`default_arguments`.`global_functions`.`lambdas`.`noinline`.`function`

fun foo(i: (Int) -> Int = { it + 1 }, k: (Int) -> Int): Int = i(0) - k(1)

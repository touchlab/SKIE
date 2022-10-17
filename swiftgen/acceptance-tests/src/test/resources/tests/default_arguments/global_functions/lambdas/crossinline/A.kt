package `tests`.`default_arguments`.`global_functions`.`lambdas`.`crossinline`

inline fun foo(crossinline i: (Int) -> Int = { it + 1 }, crossinline k: (Int) -> Int): Int = i(0) - k(1)

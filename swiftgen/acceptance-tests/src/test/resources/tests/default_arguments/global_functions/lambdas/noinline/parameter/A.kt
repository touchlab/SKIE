package `tests`.`default_arguments`.`global_functions`.`lambdas`.`noinline`.`parameter`

inline fun foo(noinline i: (Int) -> Int = { it + 1 }, noinline k: (Int) -> Int): Int = i(0) - k(1)

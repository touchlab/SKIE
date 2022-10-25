package `tests`.`default_arguments`.`global_functions`.`lambdas`.`inline`

inline fun foo(i: (Int) -> Int = { it + 1 }, k: (Int) -> Int): Int = i(0) - k(1)

package `tests`.`default_arguments`.`global_functions`.`generics`

fun <T> foo(
    defaultForDefault: T,
    defaultForReturnValue: T = defaultForDefault,
    returnValue: T = defaultForReturnValue,
): T where T : I, T : K = returnValue

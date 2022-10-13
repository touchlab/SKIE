package `tests`.`default_arguments`.`classes`.`constructors`.`generics`

class A<T>(
    defaultForDefault: T,
    defaultForValue: T = defaultForDefault,
    val value: T = defaultForValue,
) where T : I, T : K

package `tests`.`sealed`.`interfaces`.`generics`.`complex`

sealed interface A<T, U> {

    val value: U
}

class A1<K, L, M, N>(override val value: M, val value2: L, override val valueI: N) : A<K, M>, I<K, N>

sealed interface I<V, W> {
    val valueI: W
}
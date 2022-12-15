package `tests`.`sealed`.`classes`.`generics`.`complex`

sealed class A<T, U>(val value: U)

class A1<K, L, M, N>(value: M, val value2: L, override val valueI: N) : A<K, M>(value), I<K, N>

sealed interface I<V, W> {

    val valueI: W
}

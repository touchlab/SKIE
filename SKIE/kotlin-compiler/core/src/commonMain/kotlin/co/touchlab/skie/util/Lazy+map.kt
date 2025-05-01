package co.touchlab.skie.util

fun <T, R> Lazy<T>.map(transform: (T) -> R): Lazy<R> = lazy { transform(value) }

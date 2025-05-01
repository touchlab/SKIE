package co.touchlab.skie.util

sealed interface Optional<out T> {

    fun ifSome(block: (T) -> Unit)

    @JvmInline
    value class Some<out T>(val value: T) : Optional<T> {

        override fun ifSome(block: (T) -> Unit) {
            block(value)
        }
    }

    object None : Optional<Nothing> {

        override fun ifSome(block: (Nothing) -> Unit) {
            // Do nothing
        }
    }
}

fun <T> T.toOptional(): Optional<T> = Optional.Some(this)

inline fun <T> Optional<T>.orElse(defaultValue: () -> T): T = when (this) {
    is Optional.Some -> value
    Optional.None -> defaultValue()
}

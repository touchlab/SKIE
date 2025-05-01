package co.touchlab.skie.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> mutableLazy(lazyInitialValue: () -> T): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {

    private var state: State<T> = State.InitialValue(lazyInitialValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        state = State.ChangedValue(value)
    }
}

private sealed interface State<T> {

    val value: T

    class InitialValue<T>(lazyInitialValue: () -> T) : State<T> {

        override val value: T by lazy(lazyInitialValue)
    }

    class ChangedValue<T>(override val value: T) : State<T>
}

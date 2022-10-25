package co.touchlab.skie.plugin.reflection

import java.lang.reflect.Field
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class PropertyField<T : Any, V>(private val originalPropertyName: String) : ReadWriteProperty<T, V> {
    override operator fun getValue(thisRef: T, property: KProperty<*>): V = onFieldOf(thisRef) { field ->
        field.get(thisRef) as V
    }

    override operator fun setValue(thisRef: T, property: KProperty<*>, value: V) = onFieldOf(thisRef) { field ->
        field.set(thisRef, value)
    }

    private inline fun <U> onFieldOf(thisRef: T, run: (Field) -> U): U {
        thisRef.javaClass.getDeclaredField(originalPropertyName).let { field ->
            check(field.trySetAccessible()) { "Failed to make field `${originalPropertyName}` accessible" }
            @Suppress("UNCHECKED_CAST")
            return run(field)
        }
    }
}

val <T : Any, V> KProperty1<T, V>.field: PropertyField<T, V>
    get() = PropertyField<T, V>(this.name)

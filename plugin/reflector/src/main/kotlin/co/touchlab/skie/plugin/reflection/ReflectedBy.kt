package co.touchlab.skie.plugin.reflection

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

inline fun <reified T : Reflector> Any.reflectedBy(): T =
    T::class.java.constructors.first().newInstance(this) as T

@OptIn(ExperimentalContracts::class)
inline infix fun <reified T: Any> Any.matches(cls: KClass<T>): Boolean {
    contract {
        returns(true) implies (this@matches is T)
    }
    return this.javaClass.name == cls.jvmName
}

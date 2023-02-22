package co.touchlab.skie.plugin.reflection

inline fun <reified T : Reflector> Any.reflectedBy(): T =
    T::class.java.constructors.first().newInstance(this) as T

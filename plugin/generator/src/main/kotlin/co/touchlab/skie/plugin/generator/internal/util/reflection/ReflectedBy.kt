package co.touchlab.skie.plugin.generator.internal.util.reflection

internal inline fun <reified T : Reflector> Any.reflectedBy(): T =
    T::class.java.constructors.first().newInstance(this) as T

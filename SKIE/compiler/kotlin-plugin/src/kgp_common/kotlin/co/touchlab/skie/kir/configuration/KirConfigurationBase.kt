package co.touchlab.skie.kir.configuration

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class KirConfigurationBase(
    private val parent: KirConfigurationBase?,
) {

    private val delegatesByName: MutableMap<String, Property<*>> = mutableMapOf()
    private val delegatesByProperty: MutableMap<KProperty<*>, Property<*>> = mutableMapOf()

    fun reset(property: KProperty<*>) {
        val delegate = delegatesByProperty[property] ?: error("Delegate for property $property not found.")

        delegate.isInherited = true
    }

    protected fun <T> value(defaultValue: T): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> =
        PropertyDelegateProvider { _, property ->
            val delegate = Property(defaultValue)

            delegatesByName[property.name] = delegate
            delegatesByProperty[property] = delegate

            delegate
        }

    private inner class Property<T>(private var value: T) : ReadWriteProperty<Any?, T> {

        var isInherited: Boolean = true

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            if (isInherited && parent != null) {
                parent.delegatesByName.getValue(property.name).getValue(null, property) as T
            } else {
                value
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            isInherited = false

            this.value = value
        }
    }
}

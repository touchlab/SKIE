package co.touchlab.skie.configuration

import co.touchlab.skie.util.Optional
import co.touchlab.skie.util.orElse
import co.touchlab.skie.util.toOptional
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SkieConfiguration(
    private var parent: SkieConfiguration?,
) {

    open val rootConfiguration: RootConfiguration
        get() = parent?.rootConfiguration ?: error("SkieConfiguration without parent must override rootConfiguration.")

    private val delegatesByName: MutableMap<String, Property<*>> = mutableMapOf()
    private val delegatesByProperty: MutableMap<KProperty<*>, Property<*>> = mutableMapOf()

    private val keyValueConfigurationStorage: MutableMap<ConfigurationKey<*>, Any?> = mutableMapOf()

    var useDefaultsForSkieRuntime: Boolean = false

    fun overwriteBy(other: SkieConfiguration) {
        delegatesByName.clear()
        delegatesByProperty.clear()
        keyValueConfigurationStorage.clear()

        delegatesByName.putAll(other.delegatesByName)
        delegatesByProperty.putAll(other.delegatesByProperty)
        keyValueConfigurationStorage.putAll(other.keyValueConfigurationStorage)

        useDefaultsForSkieRuntime = other.useDefaultsForSkieRuntime
        parent = other.parent
    }

    fun reset(property: KProperty<*>) {
        val delegate = delegatesByProperty[property] ?: error("Delegate for property $property not found.")

        delegate.reset()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getUnsafe(configurationKey: ConfigurationKey<T>): T {
        if (configurationKey in keyValueConfigurationStorage) {
            return keyValueConfigurationStorage[configurationKey] as T
        }

        if (useDefaultsForSkieRuntime) {
            return configurationKey.skieRuntimeValue
        }

        val parent = parent
        if (parent != null && configurationKey.isInheritable) {
            return parent.getUnsafe(configurationKey)
        }

        if (isKeySupported(configurationKey)) {
            return configurationKey.defaultValue
        } else {
            error("Configuration key $configurationKey is not supported.")
        }
    }

    fun <T> setUnsafe(configurationKey: ConfigurationKey<T>, value: T) {
        keyValueConfigurationStorage[configurationKey] = value
    }

    fun <T> resetUnsafe(configurationKey: ConfigurationKey<T>) {
        keyValueConfigurationStorage.remove(configurationKey)
    }

    protected fun <T> inheritableValue(defaultValueFactory: () -> T): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> =
        createValueProperty(defaultValueFactory, true)

    protected fun <T> value(defaultValueFactory: () -> T): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> =
        createValueProperty(defaultValueFactory, false)

    protected open fun isKeySupported(key: ConfigurationKey<*>): Boolean =
        parent?.isKeySupported(key) ?: error("SkieConfiguration without parent must override isKeySupported.")

    private fun <T> SkieConfiguration.createValueProperty(
        defaultValueFactory: () -> T,
        isInheritable: Boolean,
    ): PropertyDelegateProvider<Any?, Property<T>> =
        PropertyDelegateProvider { _, property ->
            val delegate = Property(defaultValueFactory, isInheritable)

            delegatesByName[property.name] = delegate
            delegatesByProperty[property] = delegate

            delegate
        }

    private inner class Property<T>(
        defaultValueFactory: () -> T,
        private val isInheritable: Boolean,
    ) : ReadWriteProperty<Any?, T> {

        private val defaultValue: T by lazy(defaultValueFactory)

        private var value: Optional<T> = Optional.None

        var isInherited: Boolean = isInheritable

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val parent = parent

            return if (isInherited && parent != null) {
                val parentProperty = parent.delegatesByName[property.name]

                if (parentProperty != null && parentProperty.hasValue(property)) {
                    parentProperty.getValue(null, property) as T
                } else {
                    value.orElse { defaultValue }
                }
            } else {
                value.orElse { defaultValue }
            }
        }

        private fun hasValue(property: KProperty<*>): Boolean {
            val parent = parent

            return if (isInherited && parent != null) {
                parent.delegatesByName[property.name]?.hasValue(property) ?: (value is Optional.Some)
            } else {
                value is Optional.Some
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            isInherited = false

            this.value = value.toOptional()
        }

        fun reset() {
            value = Optional.None

            isInherited = isInheritable
        }
    }
}

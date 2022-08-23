package co.touchlab.swiftgen.configuration

import co.touchlab.swiftgen.BuildConfig
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ConfigurationDeclaration {

    private val optionByName = mutableMapOf<String, Option<*>>()
    private val optionByProperty = mutableMapOf<String, Option<*>>()

    val options: List<Option<*>>
        get() = optionByName.values.toList()

    fun set(name: String, value: String) {
        val option = optionByName[name] ?: throw IllegalArgumentException("Unknown option $name.")

        option.deserialize(value)
    }

    operator fun <T> get(property: KProperty<T>): Option<T> =
        @Suppress("UNCHECKED_CAST")
        (optionByProperty[property.toString()] as Option<T>?)
            ?: throw IllegalArgumentException("Property $property is not registered using `option` function.")

    fun toPluginOptions(): List<String> =
        options.map { "plugin:${BuildConfig.PLUGIN_ID}:${it.name}=${it.serialize()}" }

    fun option(
        name: String,
        defaultValue: String,
        description: String,
        valueDescription: String,
    ): PropertyDelegateProvider<Any, ReadWriteProperty<Any, String>> =
        Option.String(name, description, valueDescription, defaultValue)
            .let(::DelegateProvider)

    fun option(
        name: String,
        defaultValue: Boolean,
        description: String,
        valueDescription: String,
    ): PropertyDelegateProvider<Any, ReadWriteProperty<Any, Boolean>> =
        Option.Boolean(name, description, valueDescription, defaultValue)
            .let(::DelegateProvider)

    private inner class DelegateProvider<T>(
        private val option: Option<T>,
    ) : PropertyDelegateProvider<Any, ReadWriteProperty<Any, T>> {

        override fun provideDelegate(thisRef: Any, property: KProperty<*>): ReadWriteProperty<Any, T> {
            optionByName[option.name] = option
            optionByProperty[property.toString()] = option

            return object : ReadWriteProperty<Any, T> {

                override fun getValue(thisRef: Any, property: KProperty<*>): T =
                    option.value

                override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                    option.value = value
                }
            }
        }
    }
}

fun <S : ConfigurationDeclaration, T> S.get(selector: S.() -> KProperty<T>): Option<T> =
    get(selector())
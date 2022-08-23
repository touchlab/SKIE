package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.configuration.ConfigurationDeclaration
import co.touchlab.swiftgen.configuration.get
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PropertyProvider<C : ConfigurationDeclaration>(
    private val objects: ObjectFactory,
    configurationFactory: () -> C,
) {

    private val configuration = configurationFactory()

    private val propertyDelegates = mutableListOf<PropertyDelegate<*>>()

    fun toSubpluginOptions(): List<SubpluginOption> =
        propertyDelegates.map { it.toSubpluginOption() }

    inline fun <reified T> property(
        noinline selector: C.() -> KProperty<T>,
    ): ReadWriteProperty<Any, T> =
        PropertyDelegate(selector, T::class.java)

    inner class PropertyDelegate<T>(
        private val selector: C.() -> KProperty<T>,
        aClass: Class<T>,
    ) : ReadWriteProperty<Any, T> {

        private val property = objects.property(aClass)

        init {
            propertyDelegates.add(this)
        }

        fun toSubpluginOption(): SubpluginOption {
            val option = configuration.get(selector)

            property.orNull?.let { option.value = it }

            return SubpluginOption(option.name, option.serialize())
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): T =
            this.property.get()

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this.property.set(value)
        }
    }
}
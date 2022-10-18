package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.symboltable

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T> unsupported() = object : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        throw UnsupportedOperationException()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        throw UnsupportedOperationException()
    }
}

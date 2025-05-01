package co.touchlab.skie.kir.irbuilder.impl.symboltable

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> unsupported() = object : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T = throw UnsupportedOperationException()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T): Unit = throw UnsupportedOperationException()
}

package co.touchlab.skie.gradle.version

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class VersionContainer<VERSION: Comparable<VERSION>>(
    private val identifier: (VERSION) -> String,
) {
    private val mutableVersions = mutableListOf<VERSION>()
    val allVersions: List<VERSION> = mutableVersions

    protected fun resolve(requestedIdentifiers: List<String>): List<VERSION> {
        return requestedIdentifiers.map { requestedIdentifier ->
            val version = allVersions.find { version ->
                identifier(version) == requestedIdentifier
            } ?: throw IllegalArgumentException("Unknown version: $requestedIdentifier")
            version
        }
    }

    private fun addVersion(version: VERSION) {
        mutableVersions.add(version)
    }

    inner class VersionProvider(private val factory: (String) -> VERSION):
        PropertyDelegateProvider<Any?, ValueProperty<VERSION>> {
        override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ValueProperty<VERSION> = ValueProperty(
            factory(property.name.replace(fakePeriodCharacter, realPeriodCharacter)).also(::addVersion)
        )
    }

    class ValueProperty<VALUE>(private val value: VALUE): ReadOnlyProperty<Any?, VALUE> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): VALUE = value
    }

    companion object {
        const val fakePeriodCharacter = '․'
        const val realPeriodCharacter = '.'
    }
}

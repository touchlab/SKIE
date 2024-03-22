package co.touchlab.skie.configuration

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.runtime.belongsToSkieKotlinRuntime
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import kotlin.properties.Delegates

class ConfigurationProvider(
    private val skieConfiguration: SkieConfiguration,
) {

    private val declarationConfigurationMap = mutableMapOf<DeclarationDescriptor, DeclarationConfiguration>()

    fun <T> getDefaultConfiguration(key: ConfigurationKey<T>): T =
        skieConfiguration[NoConfigurationTarget, key]

    fun <T> getConfiguration(descriptor: DeclarationDescriptor, key: ConfigurationKey<T>): T =
        getConfiguration(descriptor)[key]

    fun inheritConfiguration(from: DeclarationDescriptor, to: DeclarationDescriptor) {
        getConfiguration(to).inheritFrom = from
    }

    fun <T> overrideConfiguration(descriptor: DeclarationDescriptor, key: ConfigurationKey<T>, value: T) {
        getConfiguration(descriptor)[key] = value
    }

    fun getConfiguration(descriptor: DeclarationDescriptor): DeclarationConfiguration =
        declarationConfigurationMap.getOrPut(descriptor) {
            DeclarationConfiguration(
                descriptor,
                skieConfiguration,
                this@ConfigurationProvider,
            )
        }

    class DeclarationConfiguration(
        private val descriptor: DeclarationDescriptor,
        private val skieConfiguration: SkieConfiguration,
        private val configurationProvider: ConfigurationProvider,
    ) {

        private val cachedValues = mutableMapOf<ConfigurationKey<*>, Any?>()

        var belongsToSkieRuntime: Boolean by Delegates.observable(descriptor.belongsToSkieKotlinRuntime) { _, _, _ ->
            cachedValues.clear()
        }

        var inheritFrom: DeclarationDescriptor? by Delegates.observable(null) { _, _, _ ->
            cachedValues.clear()
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <T> get(key: ConfigurationKey<T>): T =
            cachedValues.getOrPut(key) { loadConfiguration(key) } as T

        operator fun <T> set(key: ConfigurationKey<T>, value: T) {
            cachedValues[key] = value
        }

        private fun loadConfiguration(key: ConfigurationKey<*>): Any? =
            inheritFrom?.let { configurationProvider.getConfiguration(it, key) }
                ?: skieConfiguration[DeclarationDescriptorConfigurationTarget(descriptor, belongsToSkieRuntime), key]
    }
}

context(SkiePhase.Context)
fun DeclarationDescriptor.inheritConfiguration(from: DeclarationDescriptor) {
    configurationProvider.inheritConfiguration(from, this)
}

context(SkiePhase.Context)
fun <T> DeclarationDescriptor.overrideConfiguration(key: ConfigurationKey<T>, value: T) {
    configurationProvider.overrideConfiguration(this, key, value)
}

context(SkiePhase.Context)
var DeclarationDescriptor.belongsToSkieRuntime: Boolean
    get() = configuration.belongsToSkieRuntime
    set(value) {
        configuration.belongsToSkieRuntime = value
    }

context(SkiePhase.Context)
val DeclarationDescriptor.configuration: ConfigurationProvider.DeclarationConfiguration
    get() = configurationProvider.getConfiguration(this)

context(SkiePhase.Context)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    configuration[key]

context(SkiePhase.Context)
val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
    get() = configurationProvider.canBeUsedWithExperimentalFeatures(this)

fun ConfigurationProvider.canBeUsedWithExperimentalFeatures(descriptor: DeclarationDescriptor): Boolean =
    getConfiguration(descriptor, ExperimentalFeatures.Enabled)

fun <T> ConfigurationProvider.getConfiguration(kirClass: KirClass, key: ConfigurationKey<T>): T =
    when (kirClass.descriptor) {
        is KirClass.Descriptor.Class -> getConfiguration(kirClass.descriptor.value, key)
        is KirClass.Descriptor.File -> getDefaultConfiguration(key)
    }

context(SkiePhase.Context)
fun <T> KirClass.getConfiguration(key: ConfigurationKey<T>): T =
    configurationProvider.getConfiguration(this, key)

context(SkiePhase.Context)
fun <T> KirCallableDeclaration<*>.getConfiguration(key: ConfigurationKey<T>): T =
    configurationProvider.getConfiguration(descriptor, key)


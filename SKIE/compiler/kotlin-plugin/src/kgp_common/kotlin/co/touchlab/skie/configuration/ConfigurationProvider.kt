package co.touchlab.skie.configuration

import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

class ConfigurationProvider(
    private val context: SkiePhase.Context,
) {

    // TO:FROM
    private val inheritedConfigurationMap = mutableMapOf<DeclarationDescriptor, DeclarationDescriptor>()

    fun <T> getConfiguration(descriptor: DeclarationDescriptor, key: ConfigurationKey<T>): T =
        inheritedConfigurationMap[descriptor]?.let { getConfiguration(it, key) } ?: getUserConfiguration(descriptor, key)

    fun inheritConfiguration(from: DeclarationDescriptor, to: DeclarationDescriptor) {
        inheritedConfigurationMap[to] = from
    }

    private fun <T> getUserConfiguration(
        descriptor: DeclarationDescriptor,
        key: ConfigurationKey<T>,
    ): T =
        context.skieConfiguration[DeclarationDescriptorConfigurationTarget(descriptor), key]
}

context(SkiePhase.Context)
fun DeclarationDescriptor.inheritConfiguration(from: DeclarationDescriptor) {
    configurationProvider.inheritConfiguration(from, this)
}

context(SkiePhase.Context)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    configurationProvider.getConfiguration(this, key)

context(SkiePhase.Context)
val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
    get() = configurationProvider.canBeUsedWithExperimentalFeatures(this)

fun ConfigurationProvider.canBeUsedWithExperimentalFeatures(descriptor: DeclarationDescriptor): Boolean =
    getConfiguration(descriptor, ExperimentalFeatures.Enabled)

fun <T> ConfigurationProvider.getConfiguration(swiftModel: KotlinClassSwiftModel, key: ConfigurationKey<T>): T =
    getConfiguration(swiftModel.classDescriptor, key)

context(SkiePhase.Context)
fun <T> KotlinClassSwiftModel.getConfiguration(key: ConfigurationKey<T>): T =
    this.classDescriptor.getConfiguration(key)

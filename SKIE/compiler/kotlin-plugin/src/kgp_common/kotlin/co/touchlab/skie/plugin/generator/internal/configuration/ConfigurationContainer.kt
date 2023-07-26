package co.touchlab.skie.plugin.generator.internal.configuration

import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ExperimentalFeatures
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import co.touchlab.skie.plugin.generator.internal.configuration.getConfiguration as getConfigurationWithConfiguration

interface ConfigurationContainer {

    val skieContext: SkieContext

    val skieConfiguration: SkieConfiguration
        get() = skieContext.skieConfiguration

    fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
        with(skieConfiguration) { this@getConfiguration.getConfigurationWithConfiguration(key) }

    val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.getConfiguration(ExperimentalFeatures.Enabled)

    fun <T> KotlinClassSwiftModel.getConfiguration(key: ConfigurationKey<T>): T =
        this.classDescriptor.getConfiguration(key)

    val KotlinClassSwiftModel.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.classDescriptor.canBeUsedWithExperimentalFeatures
}

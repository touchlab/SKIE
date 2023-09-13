package co.touchlab.skie.configuration

import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import co.touchlab.skie.configuration.getConfiguration as getConfigurationWithConfiguration

interface ConfigurationContainer {

    val context: SkiePhase.Context

    val skieConfiguration: SkieConfiguration
        get() = context.skieConfiguration

    fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
        with(skieConfiguration) { this@getConfiguration.getConfigurationWithConfiguration(key) }

    val DeclarationDescriptor.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.getConfiguration(ExperimentalFeatures.Enabled)

    fun <T> KotlinClassSwiftModel.getConfiguration(key: ConfigurationKey<T>): T =
        this.classDescriptor.getConfiguration(key)

    val KotlinClassSwiftModel.canBeUsedWithExperimentalFeatures: Boolean
        get() = this.classDescriptor.canBeUsedWithExperimentalFeatures
}

package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.AnalyticsTier
import co.touchlab.skie.configuration.SkieConfigurationFlag
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieAnalyticsConfiguration @Inject constructor(objects: ObjectFactory) {

    val tier: Property<AnalyticsTier> = objects.property(AnalyticsTier::class.java).convention(AnalyticsTier.All)
    val disableUpload: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> = tier.get().configurationFlags
}

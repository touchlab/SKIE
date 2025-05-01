package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.AnalyticsTier
import co.touchlab.skie.configuration.SkieConfigurationFlag
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class SkieAnalyticsConfiguration @Inject constructor(objects: ObjectFactory) {

    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val disableUpload: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildConfigurationFlags(): Set<SkieConfigurationFlag> =
        (if (enabled.get()) AnalyticsTier.Anonymous else AnalyticsTier.None).configurationFlags
}

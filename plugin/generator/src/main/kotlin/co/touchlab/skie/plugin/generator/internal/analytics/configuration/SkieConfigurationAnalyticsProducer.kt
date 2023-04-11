package co.touchlab.skie.plugin.generator.internal.analytics.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.redacted
import co.touchlab.skie.util.redactedIfNotNumberOrBoolean
import kotlin.reflect.KClass

class SkieConfigurationAnalyticsProducer(
    private val skieConfiguration: Configuration,
) : AnalyticsProducer<AnalyticsFeature.SkieConfiguration> {

    override val featureType: KClass<AnalyticsFeature.SkieConfiguration> = AnalyticsFeature.SkieConfiguration::class

    override val name: String = "skie-configuration"

    override fun produce(configuration: AnalyticsFeature.SkieConfiguration): ByteArray =
        getSkieConfiguration(configuration).serialize().toByteArray()

    private fun getSkieConfiguration(configuration: AnalyticsFeature.SkieConfiguration): Configuration =
        if (configuration.stripIdentifiers) {
            skieConfiguration.redacted()
        } else {
            skieConfiguration
        }
}

fun Configuration.redacted(): Configuration =
    copy(
        groups = groups.map { it.redacted() },
    )

private fun Configuration.Group.redacted(): Configuration.Group =
    copy(
        targetFqNamePrefix = targetFqNamePrefix.redacted(),
        items = items.mapValues { it.value?.redactedIfNotNumberOrBoolean() },
    )

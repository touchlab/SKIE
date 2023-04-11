package co.touchlab.skie.plugin.generator.internal.analytics.system

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Command
import kotlin.reflect.KClass

object SysctlAnalyticsProducer : AnalyticsProducer<AnalyticsFeature.Sysctl> {

    override val featureType: KClass<AnalyticsFeature.Sysctl> = AnalyticsFeature.Sysctl::class

    override val name: String = "sys"

    override fun produce(configuration: AnalyticsFeature.Sysctl): ByteArray =
        Command(
            "sh",
            "-c",
            """sysctl -ae | grep -v kern.hostname""",
        )
            .execute()
            .outputLines
            .joinToString(System.lineSeparator())
            .toByteArray()
}

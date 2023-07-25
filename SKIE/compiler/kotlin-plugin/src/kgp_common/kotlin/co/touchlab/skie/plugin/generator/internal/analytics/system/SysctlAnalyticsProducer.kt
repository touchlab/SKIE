package co.touchlab.skie.plugin.generator.internal.analytics.system

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Command

object SysctlAnalyticsProducer : AnalyticsProducer {

    override val name: String = "sys"

    override val feature: SkieFeature = SkieFeature.Analytics_Sysctl

    override fun produce(): String =
        Command(
            "sh",
            "-c",
            """sysctl -ae | grep -v kern.hostname""",
        )
            .execute()
            .outputLines
            .joinToString(System.lineSeparator())
}

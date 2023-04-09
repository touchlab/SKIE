package co.touchlab.skie.plugin.generator.internal.analytics.system

import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.util.Command

object SysctlAnalyticsProducer : AnalyticsProducer {

    override fun produce(): AnalyticsProducer.Result = AnalyticsProducer.Result(
        name = "sys",
        data = run {
            Command(
                "sh",
                "-c",
                """sysctl -ae | grep -v kern.hostname"""
            ).execute().outputLines.joinToString(System.lineSeparator()).toByteArray()
        }
    )
}

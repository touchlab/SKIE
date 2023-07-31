package co.touchlab.skie.plugin.analytics.hardware

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import co.touchlab.skie.util.Command
import groovy.json.JsonSlurper
import org.apache.groovy.json.internal.JsonFastParser

internal object AnonymousHardwareAnalytics {

    object Producer : AnalyticsProducer {

        override val name: String = "anonymous-hardware"

        override val feature: SkieFeature = SkieFeature.Analytics_Anonymous_Hardware

        override fun produce(): String =
            Command("system_profiler", "SPHardwareDataType", "-detailLevel", "mini", "-json")
                .execute()
                .outputLines
                .joinToString(System.lineSeparator())
                .let { formatData(it) }

        @Suppress("UNCHECKED_CAST")
        private fun formatData(json: String): String {
            val parsedJson = JsonSlurper().parseText(json) as? Map<String, Any> ?: return "error 1"

            val array = parsedJson["SPHardwareDataType"] as? List<Any> ?: return "error 2"

            val data = array.firstOrNull() as? Map<String, Any> ?: return "error 3"

            val mutableData = data.toMutableMap()

            mutableData.remove("_name")
            mutableData.remove("boot_rom_version")
            mutableData.remove("os_loader_version")

            return mutableData.toPrettyJson()
        }
    }
}

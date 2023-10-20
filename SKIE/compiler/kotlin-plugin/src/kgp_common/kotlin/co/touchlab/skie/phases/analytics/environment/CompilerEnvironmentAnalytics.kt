@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.analytics.environment

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.analytics.util.toPrettyJson
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.target.Xcode

@Serializable
data class CompilerEnvironmentAnalytics(
    val jvmVersion: String,
    val compilerVersion: String?,
    val xcodeVersion: String,
    val availableProcessors: Int,
    val maxJvmMemory: Long,
) {

    class Producer(private val konanConfig: KonanConfig) : AnalyticsProducer {

        override val name: String = "compiler-environment"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_CompilerEnvironment

        // Xcode.findCurrent().version.toString() is not redundant because from 1.9.20 the version is not a String
        @Suppress("RemoveRedundantCallsOfConversionMethods")
        override fun produce(): String =
            CompilerEnvironmentAnalytics(
                jvmVersion = Runtime.version().toString(),
                compilerVersion = konanConfig.distribution.compilerVersion,
                xcodeVersion = Xcode.findCurrent().version.toString(),
                availableProcessors = Runtime.getRuntime().availableProcessors(),
                maxJvmMemory = Runtime.getRuntime().maxMemory(),
            ).toPrettyJson()
    }
}

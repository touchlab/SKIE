package co.touchlab.skie.analytics.compiler.common

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.BinaryOptions
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CommonConfigurationKeys

@Serializable
data class IdentifyingCommonCompilerConfigurationAnalytics(
    val bundleId: String?,
    val frameworkName: String?,
) {

    class Producer(private val config: KonanConfig) : AnalyticsProducer {

        override val name: String = "identifying-common-compiler-configuration"

        override val feature: SkieFeature = SkieFeature.Analytics_Identifying_CompilerConfiguration

        override fun produce(): String =
            IdentifyingCommonCompilerConfigurationAnalytics(
                bundleId = config.configuration.get(BinaryOptions.bundleId) ?: config.configuration.get(KonanConfigKeys.BUNDLE_ID),
                frameworkName = config.configuration.get(KonanConfigKeys.OUTPUT)?.substringAfterLast("/"),
            ).toPrettyJson()
    }
}

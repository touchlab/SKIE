package co.touchlab.skie.analytics.compiler.specific

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import org.jetbrains.kotlin.backend.konan.KonanConfig

object SpecificCompilerConfigurationAnalytics {

    class Producer(private val config: KonanConfig) : AnalyticsProducer {

        override val name: String = "specific-compiler-configuration"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_CompilerConfiguration

        override fun produce(): String = "{}"
        // Add back if needed
//             config.getSpecificCompilerConfigurationAnalytics()
    }
}

// Add back if needed
// expect fun KonanConfig.getSpecificCompilerConfigurationAnalytics(): String

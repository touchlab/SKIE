@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.analytics.compiler.specific

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import org.jetbrains.kotlin.backend.konan.KonanConfig

object AnonymousSpecificCompilerConfigurationAnalytics {

    class Producer(private val config: KonanConfig) : AnalyticsProducer {

        override val name: String = "anonymous-specific-compiler-configuration"

        override val feature: SkieFeature = SkieFeature.Analytics_Anonymous_CompilerConfiguration

        override fun produce(): String = config.getAnonymousSpecificCompilerConfigurationAnalytics()
    }
}

internal expect fun KonanConfig.getAnonymousSpecificCompilerConfigurationAnalytics(): String

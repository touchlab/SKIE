package co.touchlab.skie.plugin.generator.internal.analytics.compiler

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import org.jetbrains.kotlin.backend.konan.KonanConfig

actual class CompilerAnalyticsProducer actual constructor(
    private val config: KonanConfig,
) : AnalyticsProducer {
    override val name: String = "compiler"

    override val feature: SkieFeature = SkieFeature.Analytics_Compiler

    override fun produce(): String {
        return ""
    }
}

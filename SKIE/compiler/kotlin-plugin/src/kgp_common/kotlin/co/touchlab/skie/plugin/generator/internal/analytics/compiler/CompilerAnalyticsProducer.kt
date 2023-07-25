package co.touchlab.skie.plugin.generator.internal.analytics.compiler

import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import org.jetbrains.kotlin.backend.konan.KonanConfig

expect class CompilerAnalyticsProducer(config: KonanConfig) : AnalyticsProducer

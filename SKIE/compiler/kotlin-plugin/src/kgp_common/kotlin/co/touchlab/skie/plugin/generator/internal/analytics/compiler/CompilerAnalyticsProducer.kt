package co.touchlab.skie.plugin.generator.internal.analytics.compiler

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig

expect class CompilerAnalyticsProducer(config: KonanConfig) : AnalyticsProducer<AnalyticsFeature.Compiler>

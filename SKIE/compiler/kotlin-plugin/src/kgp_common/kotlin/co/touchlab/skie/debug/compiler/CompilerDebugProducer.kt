package co.touchlab.skie.debug.compiler

import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import org.jetbrains.kotlin.backend.konan.KonanConfig

expect class CompilerDebugProducer(config: KonanConfig) : AnalyticsProducer

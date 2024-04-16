package co.touchlab.skie.phases.analytics.compiler.specific

import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys

actual fun KonanConfig.getSpecificCompilerConfigurationAnalytics(): String =
    AnalyticsData(
        garbageCollector = configuration.get(KonanConfigKeys.GARBAGE_COLLECTOR)?.toString(),
    ).toPrettyJson()

@Serializable
private data class AnalyticsData(
    val garbageCollector: String?,
)


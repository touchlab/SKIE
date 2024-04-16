package co.touchlab.skie.analytics.compiler.specific

import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.BinaryOptions
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys

actual fun KonanConfig.getSpecificCompilerConfigurationAnalytics(): String =
    AnalyticsData(
        objcExportDisableSwiftMemberNameMangling = configuration.get(BinaryOptions.objcExportDisableSwiftMemberNameMangling),
        garbageCollector = configuration.get(KonanConfigKeys.GARBAGE_COLLECTOR)?.toString(),
    ).toPrettyJson()

@Serializable
private data class AnalyticsData(
    val objcExportDisableSwiftMemberNameMangling: Boolean?,
    val garbageCollector: String?,
)

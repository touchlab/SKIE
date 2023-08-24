package co.touchlab.skie.analytics.compiler.specific

import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.BinaryOptions
import org.jetbrains.kotlin.backend.konan.KonanConfig

actual fun KonanConfig.getSpecificCompilerConfigurationAnalytics(): String =
    AnalyticsData(
        objcExportDisableSwiftMemberNameMangling = configuration.get(BinaryOptions.objcExportDisableSwiftMemberNameMangling),
    ).toPrettyJson()

@Serializable
private data class AnalyticsData(
    val objcExportDisableSwiftMemberNameMangling: Boolean?,
)

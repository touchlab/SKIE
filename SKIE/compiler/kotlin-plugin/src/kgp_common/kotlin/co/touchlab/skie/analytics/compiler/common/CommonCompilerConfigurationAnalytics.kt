@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.analytics.compiler.common

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.util.toPrettyJson
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.backend.konan.BinaryOptions
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.HostManager

@Serializable
data class CommonCompilerConfigurationAnalytics(
    val kotlinLanguageVersion: String,
    val kotlinApiVersion: String,
    val languageVersionString: String,
    val host: String,
    val target: String,
    val overriddenProperties: Map<String, String>?,
    val isDebugBuild: Boolean?,
    val linkerArgs: List<String>?,
    val overrideClangOptions: List<String>?,
    val staticFramework: Boolean?,
    val objcGenerics: Boolean?,
    val memoryModel: String?,
    val allocationMode: String?,
    val garbageCollector: String?,
    val unitSuspendFunctionObjCExport: String?,
    val objcExportSuspendFunctionLaunchThreadRestriction: String?,
) {

    class Producer(private val config: KonanConfig) : AnalyticsProducer {

        override val name: String = "common-compiler-configuration"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_CompilerConfiguration

        override fun produce(): String =
            CommonCompilerConfigurationAnalytics(
                kotlinLanguageVersion = config.languageVersionSettings.languageVersion.toString(),
                kotlinApiVersion = config.languageVersionSettings.apiVersion.toString(),
                languageVersionString = config.languageVersionSettings.toString(),
                host = HostManager.host.name,
                target = config.target.name,
                overriddenProperties = config.configuration.get(KonanConfigKeys.OVERRIDE_KONAN_PROPERTIES),
                isDebugBuild = config.configuration.get(KonanConfigKeys.DEBUG),
                linkerArgs = config.configuration.get(KonanConfigKeys.LINKER_ARGS),
                overrideClangOptions = config.configuration.get(KonanConfigKeys.OVERRIDE_CLANG_OPTIONS),
                staticFramework = config.configuration.get(KonanConfigKeys.STATIC_FRAMEWORK),
                objcGenerics = config.configuration.get(KonanConfigKeys.OBJC_GENERICS),
                memoryModel = config.configuration.get(BinaryOptions.memoryModel)?.toString(),
                allocationMode = config.configuration.get(KonanConfigKeys.ALLOCATION_MODE)?.toString(),
                garbageCollector = config.configuration.get(KonanConfigKeys.GARBAGE_COLLECTOR)?.toString(),
                unitSuspendFunctionObjCExport = config.configuration.get(BinaryOptions.unitSuspendFunctionObjCExport)?.toString(),
                objcExportSuspendFunctionLaunchThreadRestriction = config.configuration.get(BinaryOptions.objcExportSuspendFunctionLaunchThreadRestriction)
                    ?.toString(),
            ).toPrettyJson()
    }
}

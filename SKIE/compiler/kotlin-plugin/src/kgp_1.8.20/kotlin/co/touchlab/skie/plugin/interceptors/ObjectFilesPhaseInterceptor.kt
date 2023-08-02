@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.plugin.SwiftLinkCompilePhase
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.konan.target.AppleConfigurables

internal class ObjectFilesPhaseInterceptor: SameTypePhaseInterceptor<KonanContext, Unit> {
    override fun getInterceptedPhase(): Any = objectFilesPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val config = context.config

        val generationState = context.generationState
        val namer = generationState.objCExport.namer

        val swiftObjectFiles = runSwiftLinkCompilePhase(config, context, namer)

        generationState.compilerOutput += swiftObjectFiles

        logSkiePerformance(context.skieContext)
    }

    private fun runSwiftLinkCompilePhase(
        config: KonanConfig,
        context: KonanContext,
        namer: ObjCExportNamer,
    ): List<ObjectFile> {
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()

        return SwiftLinkCompilePhase(
            config,
            context,
            namer,
        ).process(
            configurables,
            context.generationState.outputFile,
        )
    }

    private fun logSkiePerformance(context: SkieContext) {
        context.analyticsCollector.collectAsync(context.skiePerformanceAnalyticsProducer)
    }
}

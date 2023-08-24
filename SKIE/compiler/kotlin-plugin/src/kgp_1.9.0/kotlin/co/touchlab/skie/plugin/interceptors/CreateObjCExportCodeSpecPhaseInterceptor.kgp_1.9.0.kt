@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.api.model.type.translation.impl.PhaseContextSwiftTranslationProblemCollector
import co.touchlab.skie.plugin.SwiftLinkCompilePhase
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.skieDeclarationBuilder
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCExportCodeSpecPhase
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.OutputFiles
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.file.File

private object SwiftObjectFilesKey : CompilerConfigurationKey<List<ObjectFile>>("Swift object files")

var CompilerConfiguration.swiftObjectFiles: List<ObjectFile>
    get() = get(SwiftObjectFilesKey) ?: emptyList()
    set(value) = put(SwiftObjectFilesKey, value)

internal class CreateObjCExportCodeSpecPhaseInterceptor: PhaseInterceptor<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec> {
    override fun getInterceptedPhase(): Any = CreateObjCExportCodeSpecPhase

    override fun intercept(
        context: PsiToIrContext,
        input: ObjCExportedInterface,
        next: (PsiToIrContext, ObjCExportedInterface) -> ObjCExportCodeSpec,
    ): ObjCExportCodeSpec {
        context.config.skieDeclarationBuilder.declareSymbols(context.symbolTable!!)

        val codeSpec = next(context, input)

        val config = context.config
        val outputFiles = OutputFiles(config.outputPath, config.target, config.produce)
        config.configuration.swiftObjectFiles = runSwiftLinkCompilePhase(config, context, input.namer, outputFiles.mainFile)
        logSkiePerformance(context.config.configuration.skieContext)

        return codeSpec
    }

    private fun runSwiftLinkCompilePhase(
        config: KonanConfig,
        context: PsiToIrContext,
        namer: ObjCExportNamer,
        frameworkFile: File,
    ): List<ObjectFile> {
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()

        return SwiftLinkCompilePhase(
            config,
            config.configuration.skieContext,
            config.configuration.descriptorProvider,
            namer,
            problemCollector = PhaseContextSwiftTranslationProblemCollector(context),
        ).process(
            configurables,
            frameworkFile.canonicalPath,
        )
    }

    private fun logSkiePerformance(context: SkieContext) {
        context.analyticsCollector.collectAsync(context.skiePerformanceAnalyticsProducer)
    }
}

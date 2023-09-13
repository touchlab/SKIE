@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.descriptorProvider
import co.touchlab.skie.compilerinject.reflection.skieContext
import co.touchlab.skie.phases.SwiftLinkCompilePhase
import co.touchlab.skie.swiftmodel.type.translation.impl.CommonBackendContextSwiftTranslationProblemCollector
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class ObjectFilesPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = objectFilesPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val config = context.config

        val generationState = context.generationState
        val namer = generationState.objCExport.namer

        val swiftObjectFiles = runSwiftLinkCompilePhase(config, context, namer)

        generationState.compilerOutput += swiftObjectFiles
    }

    private fun runSwiftLinkCompilePhase(
        config: KonanConfig,
        context: KonanContext,
        namer: ObjCExportNamer,
    ): List<ObjectFile> {
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()

        return SwiftLinkCompilePhase(
            config = config,
            skieContext = context.skieContext,
            descriptorProvider = context.descriptorProvider,
            namer = namer,
            problemCollector = CommonBackendContextSwiftTranslationProblemCollector(context),
        ).process(
            configurables,
            context.generationState.outputFile,
        )

        return skieContext.skieBuildDirectory.swiftCompiler.objectFiles.all.map { it.absolutePath }
    }
}

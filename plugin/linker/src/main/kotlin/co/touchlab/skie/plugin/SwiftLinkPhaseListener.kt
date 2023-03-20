@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class SwiftLinkPhaseListener : PhaseInterceptor<KonanContext, Unit, Unit> {

    override val phase = PhaseInterceptor.Phase.ObjectFiles

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val config = context.config
        val generationState = context.generationState
        val namer = generationState.objCExport.namer

        val swiftObjectFiles = SwiftLinkCompilePhase(
            config,
            context,
            namer,
        ).process()

        generationState.compilerOutput += swiftObjectFiles

        context.skieContext.analyticsCollector.waitForBackgroundTasks()
    }
}

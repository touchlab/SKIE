@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.intercept

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.konan.ConfigChecks

import org.jetbrains.kotlin.backend.konan.driver.PhaseContext

import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrInput
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrOutput

import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCFrameworkInput
import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

interface PhaseInterceptor<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
    val phase: Phase<Context, Input, Output>

    fun intercept(context: Context, input: Input, next: (Context, Input) -> Output): Output

    sealed interface Phase<in Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
        sealed interface SameTypeNamed<in Context, Data>: Phase<Context, Data, Data> where Context: LoggingContext, Context: ConfigChecks
        sealed interface SimpleNamed<in Context, Input, Output>: Phase<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks

        // object FrontendPhase: SimpleNamed<FrontendContext, KotlinCoreEnvironment, FrontendPhaseOutput>

        object ProduceObjCExportInterface: SimpleNamed<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface>
        object CreateObjCFramework: SimpleNamed<PhaseContext, CreateObjCFrameworkInput, Unit>
        object CreateObjCExportCodeSpec: SimpleNamed<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec>
        object PsiToIr: SimpleNamed<PsiToIrContext, PsiToIrInput, PsiToIrOutput>

        object ObjectFiles: SameTypeNamed<KonanContext, Unit>
    }
}

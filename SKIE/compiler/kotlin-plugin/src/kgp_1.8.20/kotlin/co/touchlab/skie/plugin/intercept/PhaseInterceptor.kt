@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.intercept

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfigurationService
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.backend.konan.ConfigChecks
import org.jetbrains.kotlin.config.CompilerConfigurationKey

// interface PhaseInterceptor<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
//     val phase: Phase<Context, Input, Output>
//
//     fun intercept(context: Context, input: Input, next: (Context, Input) -> Output): Output
//
//     sealed interface Phase<in Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
//         sealed interface SameTypeNamed<in Context, Data>: Phase<Context, Data, Data> where Context: LoggingContext, Context: ConfigChecks
//         sealed interface SimpleNamed<in Context, Input, Output>: Phase<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks
//
//         // object FrontendPhase: SimpleNamed<FrontendContext, KotlinCoreEnvironment, FrontendPhaseOutput>
//
//         object ProduceObjCExportInterface: SimpleNamed<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface>
//         object CreateObjCFramework: SimpleNamed<PhaseContext, CreateObjCFrameworkInput, Unit>
//         object CreateObjCExportCodeSpec: SimpleNamed<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec>
//         object PsiToIr: SimpleNamed<PsiToIrContext, PsiToIrInput, PsiToIrOutput>
//
//         object ObjectFiles: SameTypeNamed<KonanContext, Unit>
//     }
// }

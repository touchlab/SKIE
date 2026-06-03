@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import org.jetbrains.kotlin.backend.konan.NativeSecondStageCompilationConfig
import org.jetbrains.kotlin.backend.konan.driver.NativePhaseContext

internal val NativePhaseContext.konanConfig: NativeSecondStageCompilationConfig
    get() = config as NativeSecondStageCompilationConfig

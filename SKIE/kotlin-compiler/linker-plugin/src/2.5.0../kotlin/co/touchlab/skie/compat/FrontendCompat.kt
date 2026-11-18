@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.konan.driver.phases.K1FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.config.languageVersionSettings

internal typealias SkieFrontendPhaseOutput = K1FrontendPhaseOutput

internal fun skieCreateObjCExportMapper(
    konanConfig: KonanConfig,
    frontendPhaseOutput: SkieFrontendPhaseOutput,
): ObjCExportMapper =
    ObjCExportMapper(
        konanConfig.configuration.languageVersionSettings,
        unitSuspendFunctionExport = konanConfig.unitSuspendFunctionObjCExport,
    )

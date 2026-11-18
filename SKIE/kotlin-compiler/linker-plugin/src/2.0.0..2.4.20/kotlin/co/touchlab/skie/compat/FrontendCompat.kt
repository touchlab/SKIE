@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compat

import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper

internal typealias SkieFrontendPhaseOutput = FrontendPhaseOutput.Full

internal fun skieCreateObjCExportMapper(
    konanConfig: KonanConfig,
    frontendPhaseOutput: SkieFrontendPhaseOutput,
): ObjCExportMapper =
    ObjCExportMapper(
        frontendPhaseOutput.frontendServices.deprecationResolver,
        unitSuspendFunctionExport = konanConfig.unitSuspendFunctionObjCExport,
    )

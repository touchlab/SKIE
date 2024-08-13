@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamerImpl
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportProblemCollector
import org.jetbrains.kotlin.builtins.KotlinBuiltIns

internal actual fun createObjCExportNamerImpl(
    configuration: ObjCExportNamer.Configuration,
    builtIns: KotlinBuiltIns,
    mapper: ObjCExportMapper,
    local: Boolean,
): ObjCExportNamerImpl =
    ObjCExportNamerImpl(
        configuration = configuration,
        builtIns = builtIns,
        mapper = mapper,
        problemCollector = ObjCExportProblemCollector.SILENT,
        local = local,
    )

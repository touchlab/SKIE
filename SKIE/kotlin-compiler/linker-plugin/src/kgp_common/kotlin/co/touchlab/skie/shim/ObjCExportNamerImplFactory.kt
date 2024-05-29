@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamerImpl
import org.jetbrains.kotlin.builtins.KotlinBuiltIns

internal expect fun createObjCExportNamerImpl(
    configuration: ObjCExportNamer.Configuration,
    builtIns: KotlinBuiltIns,
    mapper: ObjCExportMapper,
    local: Boolean,
): ObjCExportNamerImpl

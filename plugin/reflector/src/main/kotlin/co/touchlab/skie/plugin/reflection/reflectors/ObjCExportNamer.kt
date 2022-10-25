@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.PropertyField
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamerImpl

internal val ObjCExportNamer.mapper: ObjCExportMapper by PropertyField(ObjCExportNamerImpl::mapper.name)

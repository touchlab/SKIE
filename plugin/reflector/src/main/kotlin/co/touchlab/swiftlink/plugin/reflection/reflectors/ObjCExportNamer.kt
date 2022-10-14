@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.swiftlink.plugin.reflection.reflectors

import co.touchlab.swiftlink.plugin.reflection.PropertyField
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamerImpl

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper

internal val ObjCExportNamer.mapper: ObjCExportMapper by PropertyField(ObjCExportNamerImpl::mapper.name)

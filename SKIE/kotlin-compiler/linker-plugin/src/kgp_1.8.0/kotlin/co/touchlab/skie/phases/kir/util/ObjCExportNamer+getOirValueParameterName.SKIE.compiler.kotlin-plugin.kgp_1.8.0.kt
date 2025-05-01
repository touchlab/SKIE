package co.touchlab.skie.phases.kir.util

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

actual fun ObjCExportNamer.getKirValueParameterName(parameter: ParameterDescriptor): String = parameter.name.asString()

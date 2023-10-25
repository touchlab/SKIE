package co.touchlab.skie.phases.oir.util

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

actual fun ObjCExportNamer.getOirValueParameterName(parameter: ParameterDescriptor): String =
    getParameterName(parameter)

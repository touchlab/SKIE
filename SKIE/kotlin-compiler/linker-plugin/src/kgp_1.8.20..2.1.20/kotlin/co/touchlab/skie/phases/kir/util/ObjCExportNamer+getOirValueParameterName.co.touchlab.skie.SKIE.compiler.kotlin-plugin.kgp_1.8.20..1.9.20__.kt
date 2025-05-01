@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.phases.kir.util

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

actual fun ObjCExportNamer.getKirValueParameterName(parameter: ParameterDescriptor): String = getParameterName(parameter)

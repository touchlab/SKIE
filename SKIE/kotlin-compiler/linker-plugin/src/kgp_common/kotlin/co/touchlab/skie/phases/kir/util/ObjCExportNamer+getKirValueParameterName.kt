@file:Suppress("ktlint:standard:filename")

package co.touchlab.skie.phases.kir.util

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ParameterDescriptor

expect fun ObjCExportNamer.getKirValueParameterName(parameter: ParameterDescriptor): String

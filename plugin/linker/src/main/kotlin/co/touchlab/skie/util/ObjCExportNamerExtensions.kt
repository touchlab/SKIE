package co.touchlab.skie.util

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind

fun ObjCExportNamer.getClassSwiftName(classDescriptor: ClassDescriptor): String {
    val isEnumEntry = classDescriptor.kind == ClassKind.ENUM_ENTRY

    return if (isEnumEntry) {
        this.getEnumEntrySelector(classDescriptor)
    } else {
        this.getClassOrProtocolName(classDescriptor).swiftName
    }
}




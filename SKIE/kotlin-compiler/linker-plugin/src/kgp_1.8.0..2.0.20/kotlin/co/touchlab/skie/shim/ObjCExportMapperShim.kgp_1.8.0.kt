@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

internal actual object ObjCExportMapperShim {
    actual fun isSpecialMapped(mapper: ObjCExportMapper, descriptor: ClassDescriptor): Boolean = mapper.isSpecialMapped(descriptor)

    actual fun isTopLevel(mapper: ObjCExportMapper, descriptor: CallableMemberDescriptor): Boolean = mapper.isTopLevel(descriptor)

    actual fun getClassIfCategory(mapper: ObjCExportMapper, descriptor: CallableMemberDescriptor): ClassDescriptor? =
        mapper.getClassIfCategory(descriptor)

    actual fun isObjCProperty(mapper: ObjCExportMapper, property: PropertyDescriptor): Boolean = mapper.isObjCProperty(property)
}

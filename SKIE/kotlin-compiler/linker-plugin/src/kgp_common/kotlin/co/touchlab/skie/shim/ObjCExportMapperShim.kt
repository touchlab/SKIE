package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

internal expect object ObjCExportMapperShim {
    fun isSpecialMapped(mapper: ObjCExportMapper, descriptor: ClassDescriptor): Boolean

    fun isTopLevel(mapper: ObjCExportMapper, descriptor: CallableMemberDescriptor): Boolean

    fun getClassIfCategory(mapper: ObjCExportMapper, descriptor: CallableMemberDescriptor): ClassDescriptor?

    fun isObjCProperty(mapper: ObjCExportMapper, property: PropertyDescriptor): Boolean
}

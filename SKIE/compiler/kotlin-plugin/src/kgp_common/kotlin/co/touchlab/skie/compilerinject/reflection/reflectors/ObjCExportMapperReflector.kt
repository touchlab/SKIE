@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.backend.konan.objcexport.CustomTypeMapper
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.types.KotlinType

class ObjCExportMapperReflector(override val instance: Any) : Reflector(
    "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper",
) {

    private val extensionClass = "org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt"

    internal val getCustomTypeMapper by declaredMethod1<ClassDescriptor, CustomTypeMapper?>()

    val isBaseMethod by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    val isBaseProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val isObjCProperty by extensionFunction1<PropertyDescriptor, Boolean>(extensionClass)

    val doesThrow by extensionFunction1<FunctionDescriptor, Boolean>(extensionClass)

    private val shouldBeExposedMember by extensionFunction1<CallableMemberDescriptor, Boolean>(extensionClass, "shouldBeExposed")

    private val shouldBeExposedClass by extensionFunction1<ClassDescriptor, Boolean>(extensionClass, "shouldBeExposed")

    internal val bridgeMethod by declaredMethod1<FunctionDescriptor, MethodBridge>()

    internal val bridgeType by extensionFunction1<KotlinType, TypeBridge>(extensionClass)

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean {
        return shouldBeExposedMember(descriptor)
    }

    fun shouldBeExposed(descriptor: ClassDescriptor): Boolean {
        return shouldBeExposedClass(descriptor)
    }
}

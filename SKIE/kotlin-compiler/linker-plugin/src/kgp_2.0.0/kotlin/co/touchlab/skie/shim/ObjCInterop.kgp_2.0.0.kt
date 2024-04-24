package co.touchlab.skie.shim

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.ir.objcinterop.isExternalObjCClass
import org.jetbrains.kotlin.ir.objcinterop.isKotlinObjCClass
import org.jetbrains.kotlin.ir.objcinterop.isObjCForwardDeclaration
import org.jetbrains.kotlin.ir.objcinterop.isObjCMetaClass
import org.jetbrains.kotlin.ir.objcinterop.isObjCObjectType
import org.jetbrains.kotlin.ir.objcinterop.isObjCProtocolClass

actual fun ClassDescriptor.isExternalObjCClass(): Boolean =
    isExternalObjCClass()

actual fun ClassDescriptor.isKotlinObjCClass(): Boolean =
    isKotlinObjCClass()

actual fun ClassDescriptor.isObjCForwardDeclaration(): Boolean =
    isObjCForwardDeclaration()

actual fun ClassDescriptor.isObjCMetaClass(): Boolean =
    isObjCMetaClass()

actual fun KotlinType.isObjCObjectType(): Boolean =
    isObjCObjectType()

actual fun ClassDescriptor.isObjCProtocolClass(): Boolean =
    isObjCProtocolClass()



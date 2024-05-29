package co.touchlab.skie.shim

import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isKotlinObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.types.KotlinType

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



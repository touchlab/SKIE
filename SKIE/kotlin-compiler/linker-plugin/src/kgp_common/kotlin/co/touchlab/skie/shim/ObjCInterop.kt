package co.touchlab.skie.shim

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.types.KotlinType

expect fun ClassDescriptor.isExternalObjCClass(): Boolean

expect fun ClassDescriptor.isKotlinObjCClass(): Boolean

expect fun ClassDescriptor.isObjCForwardDeclaration(): Boolean

expect fun ClassDescriptor.isObjCMetaClass(): Boolean

// TODO Optimize by caching super types
expect fun KotlinType.isObjCObjectType(): Boolean

expect fun ClassDescriptor.isObjCProtocolClass(): Boolean

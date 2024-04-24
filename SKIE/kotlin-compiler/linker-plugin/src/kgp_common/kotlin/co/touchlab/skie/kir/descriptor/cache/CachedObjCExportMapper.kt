@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor.cache

import co.touchlab.skie.shim.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseMethods
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseProperties
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.getDeprecation
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeVisible
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.resolve.deprecation.DeprecationInfo
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass

class CachedObjCExportMapper internal constructor(
    konanConfig: KonanConfig,
    frontendServices: FrontendServices,
) {

    internal val kotlinMapper = ObjCExportMapper(frontendServices.deprecationResolver, unitSuspendFunctionExport = konanConfig.unitSuspendFunctionObjCExport)

    private val isHiddenFromObjCCache = mutableMapOf<ClassDescriptor, Boolean>()
    private val shouldBeExposedMemberCache = mutableMapOf<CallableMemberDescriptor, Boolean>()
    private val shouldBeExposedClassCache = mutableMapOf<ClassDescriptor, Boolean>()
    private val isTopLevelCache = mutableMapOf<CallableMemberDescriptor, Boolean>()
    private val getClassIfCategoryCache = mutableMapOf<CallableMemberDescriptor, ClassDescriptor?>()
    private val isBaseMethodCache = mutableMapOf<FunctionDescriptor, Boolean>()
    private val isBasePropertyCache = mutableMapOf<PropertyDescriptor, Boolean>()
    private val isObjCPropertyCache = mutableMapOf<PropertyDescriptor, Boolean>()
    private val getBaseMethodsCache = mutableMapOf<FunctionDescriptor, List<FunctionDescriptor>>()
    private val getBasePropertiesCache = mutableMapOf<PropertyDescriptor, List<PropertyDescriptor>>()
    private val bridgeMethodCache = mutableMapOf<FunctionDescriptor, MethodBridge>()
    private val deprecationCache = mutableMapOf<DeclarationDescriptor, DeprecationInfo?>()

    // Not available prior 1.9.0
    fun isHiddenFromObjC(descriptor: ClassDescriptor): Boolean =
        isHiddenFromObjCCache.getOrPut(descriptor) {
            when {
                (descriptor.containingDeclaration as? ClassDescriptor)?.let { isHiddenFromObjC(it) } == true -> true
                else -> descriptor.annotations.any { annotation ->
                    annotation.annotationClass?.annotations?.any { it.fqName == KonanFqNames.hidesFromObjC } == true
                }
            }
        }

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean =
        shouldBeExposedMemberCache.getOrPut(descriptor) {
            kotlinMapper.shouldBeExposed(descriptor)
        }

    fun shouldBeExposed(descriptor: ClassDescriptor): Boolean =
        shouldBeExposedClassCache.getOrPut(descriptor) {
            // shouldBeExposed cannot be called directly starting from Kotlin 2.0.0 because it is internal and the other overload is public
            kotlinMapper.shouldBeVisible(descriptor) && !kotlinMapper.isSpecialMapped(descriptor) && !descriptor.defaultType.isObjCObjectType()
        }

    fun isTopLevel(descriptor: CallableMemberDescriptor): Boolean =
        isTopLevelCache.getOrPut(descriptor) {
            kotlinMapper.isTopLevel(descriptor)
        }

    fun getClassIfCategory(descriptor: CallableMemberDescriptor): ClassDescriptor? =
        getClassIfCategoryCache.getOrPut(descriptor) {
            kotlinMapper.getClassIfCategory(descriptor)
        }

    fun isBaseMethod(functionDescriptor: FunctionDescriptor): Boolean =
        isBaseMethodCache.getOrPut(functionDescriptor) {
            kotlinMapper.isBaseMethod(functionDescriptor)
        }

    fun isBaseProperty(propertyDescriptor: PropertyDescriptor): Boolean =
        isBasePropertyCache.getOrPut(propertyDescriptor) {
            kotlinMapper.isBaseProperty(propertyDescriptor)
        }

    fun isObjCProperty(property: PropertyDescriptor): Boolean =
        isObjCPropertyCache.getOrPut(property) {
            kotlinMapper.isObjCProperty(property)
        }

    fun getBaseMethods(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        getBaseMethodsCache.getOrPut(descriptor) {
            kotlinMapper.getBaseMethods(descriptor)
        }

    fun getBaseProperties(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getBasePropertiesCache.getOrPut(descriptor) {
            kotlinMapper.getBaseProperties(descriptor)
        }

    internal fun bridgeMethod(descriptor: FunctionDescriptor): MethodBridge =
        bridgeMethodCache.getOrPut(descriptor) {
            kotlinMapper.bridgeMethod(descriptor)
        }

    fun getDeprecation(descriptor: DeclarationDescriptor): DeprecationInfo? =
        deprecationCache.getOrPut(descriptor) {
            kotlinMapper.getDeprecation(descriptor)
        }
}

package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.kir.type.UnresolvedFlowKirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import co.touchlab.skie.oir.type.VoidOirType
import co.touchlab.skie.shim.isExternalObjCClass
import co.touchlab.skie.shim.isKotlinObjCClass
import co.touchlab.skie.shim.isObjCForwardDeclaration
import co.touchlab.skie.shim.isObjCMetaClass
import co.touchlab.skie.shim.isObjCObjectType
import co.touchlab.skie.shim.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.isInlined
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isAny
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter

// Logic mostly copied from ObjCExportTranslatorImpl (including TODOs)
class KirTypeTranslator(private val descriptorKirProvider: DescriptorKirProvider, private val customTypeMappers: KirCustomTypeMappers) :
    KirTypeTranslatorUtilityScope() {

    context(KirTypeParameterScope)
    fun mapReferenceType(kotlinType: KotlinType): ReferenceKirType =
        mapReferenceTypeIgnoringNullability(kotlinType).withNullabilityOf(kotlinType)

    context(KirTypeParameterScope)
    fun mapReferenceTypeIgnoringNullability(kotlinType: KotlinType): NonNullReferenceKirType {
        mapTypeIfFlow(kotlinType)?.let {
            return it
        }

        customTypeMappers.mapTypeIfApplicable(kotlinType)?.let {
            return it
        }

        if (kotlinType.isTypeParameter()) {
            val genericTypeUsage = with(descriptorKirProvider) {
                getTypeParameterUsage(TypeUtils.getTypeParameterDescriptorOrNull(kotlinType))
            }

            if (genericTypeUsage != null) {
                return genericTypeUsage
            }
        }

        val classDescriptor = kotlinType.getErasedTypeClass()

        // TODO: translate `where T : BaseClass, T : SomeInterface` to `BaseClass* <SomeInterface>`

        // TODO: expose custom inline class boxes properly.
        if (isAny(classDescriptor) || classDescriptor.classId in customTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
            return SpecialReferenceOirType.Id.toKirType()
        }

        if (classDescriptor.defaultType.isObjCObjectType()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(classDescriptor)
        }

        // There are number of tricky corner cases getting here.
        val kirClass = descriptorKirProvider.findClass(classDescriptor) ?: return SpecialReferenceOirType.Id.toKirType()

        val typeArgs = if (kirClass.kind == KirClass.Kind.Interface) {
            emptyList()
        } else {
            kotlinType.arguments.map { typeProjection ->
                mapTypeArgument(typeProjection)
            }
        }

        return kirClass.toType(typeArgs)
    }

    context(KirTypeParameterScope)
    private fun mapTypeIfFlow(kotlinType: KotlinType): UnresolvedFlowKirType? {
        val supportedFlow = SupportedFlow.from(kotlinType) ?: return null

        val flowTypeArgument = kotlinType.arguments.single()

        val supportedFlowVariant = if (flowTypeArgument.type.isNullable()) {
            supportedFlow.optionalVariant
        } else {
            supportedFlow.requiredVariant
        }

        return UnresolvedFlowKirType(supportedFlowVariant) {
            mapTypeArgument(flowTypeArgument)
        }
    }

    context(KirTypeParameterScope)
    private fun KirTypeTranslator.mapTypeArgument(typeProjection: TypeProjection): NonNullReferenceKirType =
        if (typeProjection.isStarProjection) {
            SpecialReferenceOirType.Id.toKirType() // TODO: use Kotlin upper bound.
        } else {
            mapReferenceTypeIgnoringNullability(typeProjection.type)
        }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(descriptor: ClassDescriptor): NonNullReferenceKirType {
        // TODO: more precise types can be used.

        if (descriptor.isObjCMetaClass()) return SpecialReferenceOirType.Class.toKirType()
        if (descriptor.isObjCProtocolClass()) return SpecialReferenceOirType.Protocol.toKirType()

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            return descriptorKirProvider.getExternalClass(descriptor).defaultType
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny())
        }

        return SpecialReferenceOirType.Id.toKirType()
    }

    context(KirTypeParameterScope)
    fun mapFunctionType(kotlinType: KotlinType, returnsVoid: Boolean): ReferenceKirType =
        mapFunctionTypeIgnoringNullability(kotlinType, returnsVoid).withNullabilityOf(kotlinType)

    context(KirTypeParameterScope)
    fun mapFunctionTypeIgnoringNullability(functionType: KotlinType, returnsVoid: Boolean): NonNullReferenceKirType {
        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
            functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return BlockPointerKirType(
            valueParameterTypes = parameterTypes.map { mapReferenceType(it) },
            returnType = if (returnsVoid) {
                VoidOirType.toKirType()
            } else {
                mapReferenceType(functionType.getReturnTypeFromFunctionType())
            },
        )
    }

    private tailrec fun KotlinType.getErasedTypeClass(): ClassDescriptor =
        TypeUtils.getClassDescriptor(this) ?: this.constructor.supertypes.first().getErasedTypeClass()
}

package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.ErrorOutKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SuspendCompletionKirType
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.type.BlockPointerOirType
import co.touchlab.skie.oir.type.NonNullReferenceOirType
import co.touchlab.skie.oir.type.NullableReferenceOirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.ReferenceOirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import co.touchlab.skie.oir.type.VoidOirType
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isInlined
import org.jetbrains.kotlin.backend.konan.isKotlinObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isAny
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes

// Logic mostly copied from ObjCExportTranslatorImpl

class OirTypeTranslator(
    private val kirProvider: KirProvider,
    private val oirProvider: OirProvider,
    private val oirBuiltins: OirBuiltins,
    private val customTypeMappers: OirCustomTypeMappers,
) {

    fun mapType(kirType: KirType, oirTypeParameterScope: OirTypeParameterScope): OirType =
        when (kirType) {
            is OirBasedKirType -> kirType.oirType
            is ReferenceKirType -> mapType(kirType, oirTypeParameterScope)
            is BlockPointerKirType -> mapType(kirType, oirTypeParameterScope)
            is SuspendCompletionKirType -> mapType(kirType, oirTypeParameterScope)
            ErrorOutKirType -> PointerOirType(oirBuiltins.NSError.defaultType.withNullabilityOf(true), nullable = true)
        }

    private fun mapType(kirType: ReferenceKirType, oirTypeParameterScope: OirTypeParameterScope): ReferenceOirType =
        mapReferenceType(kirType.kotlinType, oirTypeParameterScope)

    private fun mapType(kirType: BlockPointerKirType, oirTypeParameterScope: OirTypeParameterScope): ReferenceOirType =
        mapFunctionTypeIgnoringNullability(kirType.kotlinType, oirTypeParameterScope, kirType.returnsVoid)
            .withNullabilityOf(kirType.kotlinType)

    private fun mapType(kirType: SuspendCompletionKirType, oirTypeParameterScope: OirTypeParameterScope): BlockPointerOirType {
        val resultType = if (kirType.useUnitCompletion) {
            null
        } else {
            when (val it = mapReferenceType(kirType.kotlinType, oirTypeParameterScope)) {
                is NonNullReferenceOirType -> NullableReferenceOirType(it, isNullableResult = false)
                is NullableReferenceOirType -> NullableReferenceOirType(it.nonNullType, isNullableResult = true)
            }
        }

        return BlockPointerOirType(
            valueParameterTypes = listOfNotNull(
                resultType,
                oirBuiltins.NSError.defaultType.withNullabilityOf(true),
            ),
            returnType = VoidOirType,
        )
    }

    private fun mapReferenceType(kotlinType: KotlinType, oirTypeParameterScope: OirTypeParameterScope): ReferenceOirType =
        mapReferenceTypeIgnoringNullability(kotlinType, oirTypeParameterScope).withNullabilityOf(kotlinType)

    fun mapReferenceTypeIgnoringNullability(kotlinType: KotlinType, oirTypeParameterScope: OirTypeParameterScope): NonNullReferenceOirType {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: OirCustomTypeMapper)

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                customTypeMappers.getMapper(descriptor)?.let { mapper ->
                    TypeMappingMatch(type, descriptor, mapper)
                }
            }
        }

        val mostSpecificMatches = typeMappingMatches.filter { match ->
            typeMappingMatches.all { otherMatch ->
                otherMatch.descriptor == match.descriptor ||
                    !otherMatch.descriptor.isSubclassOf(match.descriptor)
            }
        }

        mostSpecificMatches.firstOrNull()?.let {
            return it.mapper.mapType(it.type, oirTypeParameterScope)
        }

        if (kotlinType.isTypeParameter()) {
            val genericTypeUsage = oirTypeParameterScope.getTypeParameterUsage(TypeUtils.getTypeParameterDescriptorOrNull(kotlinType))

            if (genericTypeUsage != null)
                return genericTypeUsage
        }

        val classDescriptor = kotlinType.getErasedTypeClass()

        // TODO: translate `where T : BaseClass, T : SomeInterface` to `BaseClass* <SomeInterface>`

        // TODO: expose custom inline class boxes properly.
        if (isAny(classDescriptor) || classDescriptor.classId in customTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
            return SpecialReferenceOirType.Id
        }

        if (classDescriptor.defaultType.isObjCObjectType()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(classDescriptor)
        }

        // There are number of tricky corner cases getting here.
        val kirClass = kirProvider.findClass(classDescriptor) ?: return SpecialReferenceOirType.Id

        val typeArgs = if (kirClass.kind == KirClass.Kind.Interface) {
            emptyList()
        } else {
            kotlinType.arguments.map { typeProjection ->
                if (typeProjection.isStarProjection) {
                    SpecialReferenceOirType.Id // TODO: use Kotlin upper bound.
                } else {
                    mapReferenceTypeIgnoringNullability(typeProjection.type, oirTypeParameterScope)
                }
            }
        }

        return kirClass.oirClass.toType(typeArgs)
    }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(descriptor: ClassDescriptor): NonNullReferenceOirType {
        // TODO: more precise types can be used.

        if (descriptor.isObjCMetaClass()) return SpecialReferenceOirType.Class
        if (descriptor.isObjCProtocolClass()) return oirBuiltins.Protocol.defaultType

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            return oirProvider.getExternalClass(descriptor).defaultType
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny())
        }

        return SpecialReferenceOirType.Id
    }

    fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        oirTypeParameterScope: OirTypeParameterScope,
        returnsVoid: Boolean,
    ): BlockPointerOirType {
        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
            functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return BlockPointerOirType(
            valueParameterTypes = parameterTypes.map { mapReferenceType(it, oirTypeParameterScope) },
            returnType = if (returnsVoid) {
                VoidOirType
            } else {
                mapReferenceType(functionType.getReturnTypeFromFunctionType(), oirTypeParameterScope)
            },
        )
    }

    private fun NonNullReferenceOirType.withNullabilityOf(kotlinType: KotlinType): ReferenceOirType =
        this.withNullabilityOf(kotlinType.binaryRepresentationIsNullable())

    private fun NonNullReferenceOirType.withNullabilityOf(nullable: Boolean): ReferenceOirType =
        if (nullable) {
            NullableReferenceOirType(this)
        } else {
            this
        }

    private tailrec fun KotlinType.getErasedTypeClass(): ClassDescriptor =
        TypeUtils.getClassDescriptor(this) ?: this.constructor.supertypes.first().getErasedTypeClass()
}


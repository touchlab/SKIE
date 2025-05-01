@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor.cache

import org.jetbrains.kotlin.backend.konan.descriptors.isInterface
import org.jetbrains.kotlin.backend.konan.objcexport.BlockPointerBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.ReferenceBridge
import org.jetbrains.kotlin.backend.konan.objcexport.TypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.ValueTypeBridge
import org.jetbrains.kotlin.backend.konan.objcexport.getErasedTypeClass
import org.jetbrains.kotlin.backend.konan.objcexport.valueParametersAssociated
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.isInterface
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes

internal class ExposedDescriptorTypeVisitor(
    private val onExposedClassDescriptorVisited: (ClassDescriptor) -> Unit,
    private val mapper: CachedObjCExportMapper,
    private val objcGenerics: Boolean,
) {

    fun visitReturnType(returnBridge: MethodBridge.ReturnValue, method: FunctionDescriptor, typeParameterScope: TypeParameterScope) {
        when (returnBridge) {
            is MethodBridge.ReturnValue.Mapped -> visitType(method.returnType!!, returnBridge.bridge, typeParameterScope)
            is MethodBridge.ReturnValue.WithError.ZeroForError -> visitReturnType(returnBridge.successBridge, method, typeParameterScope)
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void,
            MethodBridge.ReturnValue.HashCode,
            MethodBridge.ReturnValue.WithError.Success,
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult,
            -> {
            }
        }
    }

    fun visitParameterTypes(baseMethodBridge: MethodBridge, method: FunctionDescriptor, typeParameterScope: TypeParameterScope) {
        baseMethodBridge.valueParametersAssociated(method).forEach { (bridge: MethodBridgeValueParameter, p: ParameterDescriptor?) ->
            when (bridge) {
                is MethodBridgeValueParameter.Mapped -> visitType(p!!.type, bridge.bridge, typeParameterScope)
                is MethodBridgeValueParameter.SuspendCompletion -> {
                    if (!bridge.useUnitCompletion) {
                        visitReferenceType(method.returnType!!, typeParameterScope)
                    }
                }
                MethodBridgeValueParameter.ErrorOutParameter -> {
                }
            }
        }
    }

    fun visitSuperClassTypeArguments(classDescriptor: ClassDescriptor) {
        if (objcGenerics) {
            val typeParameterScope = TypeParameterRootScope.deriveFor(classDescriptor)

            computeSuperClassType(classDescriptor)?.let { parentType ->
                parentType.arguments.map { typeProjection ->
                    visitReferenceType(typeProjection.type, typeParameterScope)
                }
            }
        }
    }

    private fun visitType(kotlinType: KotlinType, typeBridge: TypeBridge, typeParameterScope: TypeParameterScope) {
        when (typeBridge) {
            ReferenceBridge -> visitReferenceType(kotlinType, typeParameterScope)
            is BlockPointerBridge -> visitFunctionType(kotlinType, typeParameterScope, typeBridge)
            is ValueTypeBridge -> {}
        }
    }

    private fun computeSuperClassType(descriptor: ClassDescriptor): KotlinType? =
        descriptor.typeConstructor.supertypes.firstOrNull { !it.isInterface() }

    fun visitReferenceType(kotlinType: KotlinType, typeParameterScope: TypeParameterScope) {
        class TypeMappingMatch(
            val type: KotlinType,
            val descriptor: ClassDescriptor,
            val visitor: ExposedDescriptorCustomTypeVisitors.Visitor,
        )

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                ExposedDescriptorCustomTypeVisitors.getVisitor(descriptor)?.let { mapper ->
                    TypeMappingMatch(type, descriptor, mapper)
                }
            }
        }

        typeMappingMatches
            .firstOrNull { match ->
                typeMappingMatches.all { otherMatch ->
                    otherMatch.descriptor == match.descriptor ||
                        !otherMatch.descriptor.isSubclassOf(match.descriptor)
                }
            }
            ?.let {
                typeParameterScope.deriveFor(it.type)?.let { typeParameterScope ->
                    it.visitor.visitType(it.type, this, typeParameterScope)
                } ?: return
            }

        if (objcGenerics &&
            kotlinType.isTypeParameter() &&
            typeParameterScope.isTypeParameterUsage(TypeUtils.getTypeParameterDescriptorOrNull(kotlinType))
        ) {
            return
        }

        val classDescriptor = kotlinType.getErasedTypeClass()

        if (!mapper.shouldBeExposed(classDescriptor) || classDescriptor.classId in mapper.kotlinMapper.hiddenTypes) {
            return
        }

        if (!classDescriptor.isInterface) {
            kotlinType.arguments.map { typeProjection ->
                if (!typeProjection.isStarProjection) {
                    visitReferenceType(typeProjection.type, typeParameterScope)
                }
            }
        }

        onExposedClassDescriptorVisited(classDescriptor)
    }

    private fun visitFunctionType(kotlinType: KotlinType, typeParameterScope: TypeParameterScope, typeBridge: BlockPointerBridge) {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType
        }

        visitFunctionType(functionType, typeParameterScope, typeBridge.returnsVoid)
    }

    fun visitFunctionType(functionType: KotlinType, typeParameterScope: TypeParameterScope, returnsVoid: Boolean) {
        functionType.getReceiverTypeFromFunctionType()?.let {
            visitReferenceType(it, typeParameterScope)
        }

        functionType.getValueParameterTypesFromFunctionType().forEach {
            visitReferenceType(it.type, typeParameterScope)
        }

        if (!returnsVoid) {
            visitReferenceType(functionType.getReturnTypeFromFunctionType(), typeParameterScope)
        }
    }

    interface TypeParameterScope {

        val parent: TypeParameterScope?

        fun isTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): Boolean =
            parent?.isTypeParameterUsage(typeParameterDescriptor) ?: false

        fun wasTypeAlreadyVisited(type: KotlinType): Boolean = parent?.wasTypeAlreadyVisited(type) ?: false

        fun deriveFor(type: KotlinType): TypeParameterTypeScope? = TypeParameterTypeScope(this, type)

        fun deriveFor(classDescriptor: ClassDescriptor): TypeParameterScope = if (classDescriptor.kind.isInterface) {
            TypeParameterRootScope
        } else {
            TypeParameterClassScope(classDescriptor, this)
        }
    }

    object TypeParameterRootScope : TypeParameterScope {

        override val parent: TypeParameterScope? = null
    }

    class TypeParameterClassScope(classDescriptor: ClassDescriptor, override val parent: TypeParameterScope) : TypeParameterScope {

        private val typeParameterNames = classDescriptor.typeConstructor.parameters

        override fun isTypeParameterUsage(typeParameterDescriptor: TypeParameterDescriptor?): Boolean =
            typeParameterDescriptor?.let { descriptor ->
                typeParameterNames.firstOrNull {
                    it == descriptor || it.isCapturedFromOuterDeclaration && it.original == descriptor
                }
            } != null
    }

    class TypeParameterTypeScope private constructor(override val parent: TypeParameterScope, private val type: KotlinType) :
        TypeParameterScope {

        override fun wasTypeAlreadyVisited(type: KotlinType): Boolean = type == this.type || super.wasTypeAlreadyVisited(type)

        companion object {

            operator fun invoke(parent: TypeParameterScope, type: KotlinType): TypeParameterTypeScope? =
                if (parent.wasTypeAlreadyVisited(type)) {
                    null
                } else {
                    TypeParameterTypeScope(parent, type)
                }
        }
    }
}

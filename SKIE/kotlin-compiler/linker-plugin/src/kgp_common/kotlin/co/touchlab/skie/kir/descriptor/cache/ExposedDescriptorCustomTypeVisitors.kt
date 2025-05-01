@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor.cache

import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

internal object ExposedDescriptorCustomTypeVisitors {

    private val predefined: Map<ClassId, Visitor> = with(StandardNames.FqNames) {
        val result = mutableListOf<Visitor>()

        result += Collection(list)
        result += Collection(mutableList)
        result += Collection(set)
        result += Collection(mutableSet)
        result += Collection(map)
        result += Collection(mutableMap)

        NSNumberKind.values().forEach {
            val classId = it.mappedKotlinClassId
            if (classId != null) {
                result += Simple(classId)
            }
        }

        result += Simple(ClassId.topLevel(string.toSafe()))

        result.associateBy { it.exposedClassId }
    }

    fun getVisitor(descriptor: ClassDescriptor): Visitor? {
        val classId = descriptor.classId

        predefined[classId]?.let { return it }

        if (descriptor.isMappedFunctionClass()) {
            val arity = descriptor.declaredTypeParameters.size - 1
            assert(classId == StandardNames.getFunctionClassId(arity))
            return Function(arity)
        }

        return null
    }

    interface Visitor {

        val exposedClassId: ClassId

        fun visitType(
            mappedSuperType: KotlinType,
            visitor: ExposedDescriptorTypeVisitor,
            typeParameterScope: ExposedDescriptorTypeVisitor.TypeParameterScope,
        )
    }

    private class Simple(override val exposedClassId: ClassId) : Visitor {

        override fun visitType(
            mappedSuperType: KotlinType,
            visitor: ExposedDescriptorTypeVisitor,
            typeParameterScope: ExposedDescriptorTypeVisitor.TypeParameterScope,
        ) {
        }
    }

    private class Collection(exposedClassFqName: FqName) : Visitor {

        override val exposedClassId = ClassId.topLevel(exposedClassFqName)

        override fun visitType(
            mappedSuperType: KotlinType,
            visitor: ExposedDescriptorTypeVisitor,
            typeParameterScope: ExposedDescriptorTypeVisitor.TypeParameterScope,
        ) {
            mappedSuperType.arguments.forEach {
                if (!TypeUtils.isNullableType(it.type)) {
                    visitor.visitReferenceType(it.type, typeParameterScope)
                }
            }
        }
    }

    private class Function(parameterCount: Int) : Visitor {

        override val exposedClassId: ClassId = StandardNames.getFunctionClassId(parameterCount)

        override fun visitType(
            mappedSuperType: KotlinType,
            visitor: ExposedDescriptorTypeVisitor,
            typeParameterScope: ExposedDescriptorTypeVisitor.TypeParameterScope,
        ) = visitor.visitFunctionType(mappedSuperType, typeParameterScope, returnsVoid = false)
    }
}

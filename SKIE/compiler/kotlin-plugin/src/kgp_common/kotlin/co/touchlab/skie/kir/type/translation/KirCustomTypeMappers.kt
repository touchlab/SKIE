@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.SpecialOirKirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.supertypes

class KirCustomTypeMappers(
    private val kirBuiltins: KirBuiltins,
    translator: Lazy<KirTypeTranslator>,
) {

    private val translator by translator

    /**
     * Custom type mappers.
     *
     * Don't forget to update [hiddenTypes] after adding new one.
     */
    private val predefined: Map<ClassId, KirCustomTypeMapper> by lazy {
        with(StandardNames.FqNames) {
            val result = mutableListOf<KirCustomTypeMapper>()

            result += Collection(list, kirBuiltins.NSArray)
            result += Collection(mutableList, kirBuiltins.NSMutableArray)
            result += Collection(set, kirBuiltins.NSSet)
            result += Collection(mutableSet, kirBuiltins.MutableSet)
            result += Collection(map, kirBuiltins.NSDictionary)
            result += Collection(mutableMap, kirBuiltins.MutableMap)

            kirBuiltins.nsNumberDeclarations.forEach { (classId, kirClass) ->
                result += Simple(classId, kirClass)
            }

            result += Simple(ClassId.topLevel(string.toSafe()), kirBuiltins.NSString)

            result.associateBy { it.mappedClassId }
        }
    }

    context(KirTypeParameterScope)
    fun mapTypeIfApplicable(kotlinType: KotlinType): NonNullReferenceKirType? {
        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                getMapper(descriptor)?.let { mapper ->
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

        return mostSpecificMatches.firstOrNull()?.let {
            withTypeParameterScopeFor(it.type) { it.mapper.mapType(it.type) } ?: SpecialOirKirType(SpecialReferenceOirType.Id)
        }
    }

    private fun getMapper(descriptor: ClassDescriptor): KirCustomTypeMapper? {
        val classId = descriptor.classId

        predefined[classId]?.let { return it }

        if (descriptor.isMappedFunctionClass()) {
            // TODO: somewhat hacky, consider using FunctionClassDescriptor.arity later.
            val arity = descriptor.declaredTypeParameters.size - 1 // Type parameters include return type.
            assert(classId == StandardNames.getFunctionClassId(arity))
            return Function(arity)
        }

        return null
    }

    /**
     * Types to be "hidden" during mapping, i.e. represented as `id`.
     *
     * Currently contains super types of classes handled by custom type mappers.
     * Note: can be generated programmatically, but requires stdlib in this case.
     */
    val hiddenTypes: Set<ClassId> = listOf(
        "kotlin.Any",
        "kotlin.CharSequence",
        "kotlin.Comparable",
        "kotlin.Function",
        "kotlin.Number",
        "kotlin.collections.Collection",
        "kotlin.collections.Iterable",
        "kotlin.collections.MutableCollection",
        "kotlin.collections.MutableIterable",
    ).map { ClassId.topLevel(FqName(it)) }.toSet()

    private class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: KirCustomTypeMapper)

    private inner class Simple(
        override val mappedClassId: ClassId,
        private val kirClass: KirClass,
    ) : KirCustomTypeMapper {

        context(KirTypeParameterScope)
        override fun mapType(mappedSuperType: KotlinType): NonNullReferenceKirType =
            kirClass.defaultType
    }

    private inner class Collection(
        mappedClassFqName: FqName,
        private val kirClass: KirClass,
    ) : KirCustomTypeMapper {

        override val mappedClassId = ClassId.topLevel(mappedClassFqName)

        context(KirTypeParameterScope)
        override fun mapType(
            mappedSuperType: KotlinType,
        ): NonNullReferenceKirType {
            val typeArguments = mappedSuperType.arguments.map {
                val argument = it.type
                if (TypeUtils.isNullableType(argument)) {
                    // Kotlin `null` keys and values are represented as `NSNull` singleton.
                    SpecialOirKirType(SpecialReferenceOirType.Id)
                } else {
                    translator.mapReferenceTypeIgnoringNullability(argument)
                }
            }

            return kirClass.toType(typeArguments)
        }
    }

    private inner class Function(private val parameterCount: Int) : KirCustomTypeMapper {

        override val mappedClassId: ClassId
            get() = StandardNames.getFunctionClassId(parameterCount)

        context(KirTypeParameterScope)
        override fun mapType(
            mappedSuperType: KotlinType,
        ): NonNullReferenceKirType =
            translator.mapFunctionTypeIgnoringNullability(mappedSuperType, returnsVoid = false)
    }
}

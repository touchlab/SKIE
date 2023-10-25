@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.type.NonNullReferenceOirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

class OirCustomTypeMappers(
    private val kirBuiltins: KirBuiltins,
    private val oirBuiltins: OirBuiltins,
    translator: Lazy<OirTypeTranslator>,
) {

    private val translator by translator

    /**
     * Custom type mappers.
     *
     * Don't forget to update [hiddenTypes] after adding new one.
     */
    private val predefined: Map<ClassId, OirCustomTypeMapper> by lazy {
        with(StandardNames.FqNames) {
            val result = mutableListOf<OirCustomTypeMapper>()

            result += Collection(list, oirBuiltins.NSArray)
            result += Collection(mutableList, oirBuiltins.NSMutableArray)
            result += Collection(set, oirBuiltins.NSSet)
            result += Collection(mutableSet, kirBuiltins.MutableSet.oirClass)
            result += Collection(map, oirBuiltins.NSDictionary)
            result += Collection(mutableMap, kirBuiltins.MutableMap.oirClass)

            kirBuiltins.nsNumberDeclarations.forEach { (classId, kirClass) ->
                result += Simple(classId, kirClass.oirClass)
            }

            result += Simple(ClassId.topLevel(string.toSafe()), oirBuiltins.NSString)

            result.associateBy { it.mappedClassId }
        }
    }

    fun getMapper(descriptor: ClassDescriptor): OirCustomTypeMapper? {
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

    private inner class Simple(
        override val mappedClassId: ClassId,
        private val oirClass: OirClass,
    ) : OirCustomTypeMapper {

        override fun mapType(mappedSuperType: KotlinType, oirTypeParameterScope: OirTypeParameterScope): NonNullReferenceOirType =
            oirClass.defaultType
    }

    private inner class Collection(
        mappedClassFqName: FqName,
        private val oirClass: OirClass,
    ) : OirCustomTypeMapper {

        override val mappedClassId = ClassId.topLevel(mappedClassFqName)

        override fun mapType(
            mappedSuperType: KotlinType,
            oirTypeParameterScope: OirTypeParameterScope,
        ): NonNullReferenceOirType {
            val typeArguments = mappedSuperType.arguments.map {
                val argument = it.type
                if (TypeUtils.isNullableType(argument)) {
                    // Kotlin `null` keys and values are represented as `NSNull` singleton.
                    SpecialReferenceOirType.Id
                } else {
                    translator.mapReferenceTypeIgnoringNullability(argument, oirTypeParameterScope)
                }
            }

            return oirClass.toType(typeArguments)
        }
    }

    private inner class Function(private val parameterCount: Int) : OirCustomTypeMapper {

        override val mappedClassId: ClassId
            get() = StandardNames.getFunctionClassId(parameterCount)

        override fun mapType(
            mappedSuperType: KotlinType,
            oirTypeParameterScope: OirTypeParameterScope,
        ): NonNullReferenceOirType =
            translator.mapFunctionTypeIgnoringNullability(mappedSuperType, oirTypeParameterScope, returnsVoid = false)
    }
}

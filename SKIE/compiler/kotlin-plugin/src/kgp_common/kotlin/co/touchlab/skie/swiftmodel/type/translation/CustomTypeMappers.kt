@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.swiftmodel.type.translation

import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.NonNullSirType
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

class CustomTypeMappers(
    private val sirBuiltins: SirBuiltins,
) {

    /**
     * Custom type mappers.
     *
     * Don't forget to update [hiddenTypes] after adding new one.
     */
    private val predefined: Map<ClassId, CustomTypeMapper> = with(StandardNames.FqNames) {
        val result = mutableListOf<CustomTypeMapper>()

        result += ListMapper
        result += Simple(ClassId.topLevel(mutableList)) { sirBuiltins.Foundation.NSMutableArray.defaultType }
        result += SetMapper
        result += Collection(mutableSet) { sirBuiltins.Kotlin.MutableSet.toType(it) }
        result += MapMapper
        result += Collection(mutableMap) { sirBuiltins.Kotlin.MutableMap.toType(it) }

        NSNumberKind.values().forEach {
            // TODO: NSNumber seem to have different equality semantics.
            val classId = it.mappedKotlinClassId
            if (classId != null) {
                result += Simple(classId) { sirBuiltins.Kotlin.nsNumberDeclarations.getValue(classId).defaultType }
            }
        }

        result += StringMapper // Simple(ClassId.topLevel(string.toSafe()), "String") // "NSString")

        result.associateBy { it.mappedClassId }
    }

    fun getMapper(descriptor: ClassDescriptor): CustomTypeMapper? {
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

    private object StringMapper : CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.string.toSafe())

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType =
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> sirBuiltins.Foundation.NSString.defaultType
                else -> sirBuiltins.Swift.String.defaultType
            }
    }

    private object ListMapper : CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.list)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> sirBuiltins.Swift.AnyHashable.defaultType
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> sirBuiltins.Foundation.NSArray.defaultType
                else -> {
                    val typeArguments = mappedSuperType.arguments.map {
                        val argument = it.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            SpecialSirType.Any
                        } else {
                            translator.mapReferenceTypeIgnoringNullability(
                                argument,
                                swiftExportScope,
                                flowMappingStrategy.forTypeArgumentsOf(mappedSuperType),
                            )
                        }
                    }

                    sirBuiltins.Swift.Array.toType(typeArguments)
                }
            }
        }
    }

    private object SetMapper : CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.set)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> sirBuiltins.Foundation.NSSet.defaultType
                else -> {
                    val typeArguments = mappedSuperType.arguments.map {
                        val argument = it.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            sirBuiltins.Swift.AnyHashable.defaultType
                        } else {
                            translator.mapReferenceTypeIgnoringNullability(
                                argument,
                                swiftExportScope.addingFlags(SwiftExportScope.Flags.Hashable),
                                flowMappingStrategy.forTypeArgumentsOf(mappedSuperType),
                            )
                        }
                    }

                    sirBuiltins.Swift.Set.toType(typeArguments)
                }
            }
        }
    }

    private object MapMapper : CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.map)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> sirBuiltins.Swift.AnyHashable.defaultType
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> sirBuiltins.Foundation.NSDictionary.defaultType
                else -> {
                    val typeArguments = mappedSuperType.arguments.mapIndexed { index, typeProjection ->
                        val argument = typeProjection.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            if (index == 0) {
                                sirBuiltins.Swift.AnyHashable.defaultType
                            } else {
                                SpecialSirType.Any
                            }
                        } else {
                            val argumentScope = if (index == 0) {
                                swiftExportScope.addingFlags(SwiftExportScope.Flags.Hashable)
                            } else {
                                swiftExportScope
                            }

                            translator.mapReferenceTypeIgnoringNullability(
                                argument,
                                argumentScope,
                                flowMappingStrategy.forTypeArgumentsOf(mappedSuperType),
                            )
                        }
                    }

                    sirBuiltins.Swift.Dictionary.toType(typeArguments)
                }

            }
        }
    }

    private class Simple(
        override val mappedClassId: ClassId,
        private val getType: SwiftTypeTranslator.() -> NonNullSirType,
    ) : CustomTypeMapper {

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType =
            translator.getType()
    }

    private class Collection(
        mappedClassFqName: FqName,
        private val getType: SwiftTypeTranslator.(typeArguments: List<NonNullSirType>) -> NonNullSirType,
    ) : CustomTypeMapper {

        override val mappedClassId = ClassId.topLevel(mappedClassFqName)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            val typeArguments = mappedSuperType.arguments.map {
                val argument = it.type
                if (TypeUtils.isNullableType(argument)) {
                    // Kotlin `null` keys and values are represented as `NSNull` singleton.
                    sirBuiltins.Swift.AnyObject.defaultType
                } else {
                    translator.mapReferenceTypeIgnoringNullability(
                        argument,
                        swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType),
                        flowMappingStrategy.forTypeArgumentsOf(mappedSuperType),
                    )
                }
            }

            return translator.getType(typeArguments)
        }
    }

    private class Function(private val parameterCount: Int) : CustomTypeMapper {

        override val mappedClassId: ClassId
            get() = StandardNames.getFunctionClassId(parameterCount)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): NonNullSirType {
            return translator.mapFunctionTypeIgnoringNullability(
                mappedSuperType,
                swiftExportScope,
                returnsVoid = false,
                flowMappingStrategy.forTypeArgumentsOf(mappedSuperType),
            )
        }
    }
}

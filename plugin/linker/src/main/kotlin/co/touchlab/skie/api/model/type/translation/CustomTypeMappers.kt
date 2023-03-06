@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyObjectTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

object CustomTypeMappers {

    /**
     * Custom type mappers.
     *
     * Don't forget to update [hiddenTypes] after adding new one.
     */
    private val predefined: Map<ClassId, CustomTypeMapper> = with(StandardNames.FqNames) {
        val result = mutableListOf<CustomTypeMapper>()

        result += ListMapper
        result += Simple(ClassId.topLevel(mutableList), "NSMutableArray")
        result += SetMapper
        result += Collection(mutableSet) { namer.mutableSetName.swiftName }
        result += MapMapper
        result += Collection(mutableMap) { namer.mutableMapName.swiftName }

        NSNumberKind.values().forEach {
            // TODO: NSNumber seem to have different equality semantics.
            val classId = it.mappedKotlinClassId
            if (classId != null) {
                result += Simple(classId) { namer.numberBoxName(classId).swiftName }
            }
        }

        result += StringMapper // Simple(ClassId.topLevel(string.toSafe()), "String") // "NSString")

        result.associateBy { it.mappedClassId }
    }

    internal val functionTypeMappersArityLimit = 33 // not including, i.e. [0..33)

    fun hasMapper(descriptor: ClassDescriptor): Boolean {
        // Should be equivalent to `getMapper(descriptor) != null`.
        if (descriptor.classId in predefined) return true
        if (descriptor.isMappedFunctionClass()) return true
        return false
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
        "kotlin.collections.MutableIterable"
    ).map { ClassId.topLevel(FqName(it)) }.toSet()

    private object StringMapper : CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.string.toSafe())

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): SwiftNonNullReferenceTypeModel {
            return SwiftClassTypeModel(
                when {
                    swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> "NSString"
                    else -> "String"
                }
            )
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
        ): SwiftNonNullReferenceTypeModel {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftClassTypeModel("NSArray")
                else -> {
                    val typeArguments = mappedSuperType.arguments.map {
                        val argument = it.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            SwiftAnyTypeModel
                        } else {
                            translator.mapReferenceTypeIgnoringNullability(argument, swiftExportScope, flowMappingStrategy.forGenerics())
                        }
                    }

                    SwiftClassTypeModel("Array", typeArguments)
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
        ): SwiftNonNullReferenceTypeModel {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftClassTypeModel("NSSet")
                else -> {
                    val typeArguments = mappedSuperType.arguments.map {
                        val argument = it.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            SwiftAnyHashableTypeModel
                        } else {
                            translator.mapReferenceTypeIgnoringNullability(
                                argument,
                                swiftExportScope.addingFlags(SwiftExportScope.Flags.Hashable),
                                flowMappingStrategy.forGenerics(),
                            )
                        }
                    }

                    SwiftClassTypeModel("Set", typeArguments)
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
        ): SwiftNonNullReferenceTypeModel {
            return when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
                swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftClassTypeModel("NSDictionary")
                else -> {
                    val typeArguments = mappedSuperType.arguments.mapIndexed { index, typeProjection ->
                        val argument = typeProjection.type
                        if (TypeUtils.isNullableType(argument)) {
                            // Kotlin `null` keys and values are represented as `NSNull` singleton.
                            if (index == 0) {
                                SwiftAnyHashableTypeModel
                            } else {
                                SwiftAnyTypeModel
                            }
                        } else {
                            val argumentScope = if (index == 0) {
                                swiftExportScope.addingFlags(SwiftExportScope.Flags.Hashable)
                            } else {
                                swiftExportScope
                            }
                            translator.mapReferenceTypeIgnoringNullability(argument, argumentScope, flowMappingStrategy.forGenerics())
                        }
                    }

                    SwiftClassTypeModel("Dictionary", typeArguments)
                }

            }
        }
    }

    private class Simple(
        override val mappedClassId: ClassId,
        private val getObjCClassName: SwiftTypeTranslator.() -> String,
    ) : CustomTypeMapper {

        constructor(
            mappedClassId: ClassId,
            objCClassName: String,
        ) : this(mappedClassId, { objCClassName })

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): SwiftNonNullReferenceTypeModel =
            SwiftClassTypeModel(translator.getObjCClassName())
    }

    private class Collection(
        mappedClassFqName: FqName,
        private val getObjCClassName: SwiftTypeTranslator.() -> String,
    ) : CustomTypeMapper {

        constructor(
            mappedClassFqName: FqName,
            objCClassName: String,
        ) : this(mappedClassFqName, { objCClassName })

        override val mappedClassId = ClassId.topLevel(mappedClassFqName)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
            flowMappingStrategy: FlowMappingStrategy,
        ): SwiftNonNullReferenceTypeModel {
            val typeArguments = mappedSuperType.arguments.map {
                val argument = it.type
                if (TypeUtils.isNullableType(argument)) {
                    // Kotlin `null` keys and values are represented as `NSNull` singleton.
                    SwiftAnyObjectTypeModel
                } else {
                    translator.mapReferenceTypeIgnoringNullability(
                        argument,
                        swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType),
                        flowMappingStrategy.forGenerics(),
                    )
                }
            }

            return SwiftClassTypeModel(translator.getObjCClassName(), typeArguments)
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
        ): SwiftNonNullReferenceTypeModel {
            return translator.mapFunctionTypeIgnoringNullability(
                mappedSuperType,
                swiftExportScope,
                returnsVoid = false,
                flowMappingStrategy.forGenerics(),
            )
        }
    }
}

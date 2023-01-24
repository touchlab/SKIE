@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.SwiftTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.translation.ObjCValueType
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyHashableTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyObjectTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftAnyTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftErrorTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftInstanceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeProtocolTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftLambdaTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftMetaClassTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNullableRefefenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPointerTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftPrimitiveTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftProtocolTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftRawTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftReferenceTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftTypeModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftVoidTypeModel
import co.touchlab.skie.plugin.api.util.isInterface
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.binaryRepresentationIsNullable
import org.jetbrains.kotlin.backend.konan.isExternalObjCClass
import org.jetbrains.kotlin.backend.konan.isInlined
import org.jetbrains.kotlin.backend.konan.isKotlinObjCClass
import org.jetbrains.kotlin.backend.konan.isObjCForwardDeclaration
import org.jetbrains.kotlin.backend.konan.isObjCMetaClass
import org.jetbrains.kotlin.backend.konan.isObjCObjectType
import org.jetbrains.kotlin.backend.konan.isObjCProtocolClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.isMappedFunctionClass
import org.jetbrains.kotlin.backend.konan.reportCompilationWarning
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.getReceiverTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getReturnTypeFromFunctionType
import org.jetbrains.kotlin.builtins.getValueParameterTypesFromFunctionType
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithSource
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isSubclassOf
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind as InternalNSNumberKind

fun SwiftTypeModel.makeNullableIfReferenceOrPointer(): SwiftTypeModel = when (this) {
    is SwiftPointerTypeModel -> SwiftPointerTypeModel(pointee, nullable = true)
    is SwiftNonNullReferenceTypeModel -> SwiftNullableRefefenceTypeModel(this)
    is SwiftNullableRefefenceTypeModel, is SwiftRawTypeModel, is SwiftPrimitiveTypeModel, SwiftVoidTypeModel, SwiftErrorTypeModel -> this
}



interface SwiftTranslationProblemCollector {
    fun reportWarning(text: String)
    fun reportWarning(method: FunctionDescriptor, text: String)
    fun reportException(throwable: Throwable)

    object SILENT : SwiftTranslationProblemCollector {
        override fun reportWarning(text: String) {}
        override fun reportWarning(method: FunctionDescriptor, text: String) {}
        override fun reportException(throwable: Throwable) {}
    }

    class Default(val context: CommonBackendContext) : SwiftTranslationProblemCollector {
        override fun reportWarning(text: String) {
            context.reportCompilationWarning(text)
        }

        override fun reportWarning(method: FunctionDescriptor, text: String) {
            val psi = (method as? DeclarationDescriptorWithSource)?.source?.getPsi()
                ?: return reportWarning(
                    "$text\n    (at ${DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(method)})"
                )

            val location = MessageUtil.psiElementToMessageLocation(psi)

            context.configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
                .report(CompilerMessageSeverity.WARNING, text, location)
        }

        override fun reportException(throwable: Throwable) {
            throw throwable
        }
    }
}

internal tailrec fun KotlinType.getErasedTypeClass(): ClassDescriptor =
    TypeUtils.getClassDescriptor(this) ?: this.constructor.supertypes.first().getErasedTypeClass()


internal interface CustomTypeMapper {
    val mappedClassId: ClassId

    context(SwiftModelScope)
    fun mapType(mappedSuperType: KotlinType, translator: SwiftTypeTranslator, swiftExportScope: SwiftExportScope): SwiftNonNullReferenceTypeModel
}

internal object CustomTypeMappers {
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
        result += Collection(mutableSet) { namer.mutableSetName.objCName }
        result += MapMapper
        result += Collection(mutableMap) { namer.mutableMapName.objCName }

        InternalNSNumberKind.values().forEach {
            // TODO: NSNumber seem to have different equality semantics.
            val classId = it.mappedKotlinClassId
            if (classId != null) {
                result += Simple(classId) { namer.numberBoxName(classId).objCName }
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

    private object StringMapper: CustomTypeMapper {

        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.string.toSafe())

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope
        ): SwiftNonNullReferenceTypeModel {
            return SwiftClassTypeModel(
                when {
                    swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> "NSString"
                    else -> "String"
                }
            )
        }
    }

    private object ListMapper: CustomTypeMapper {
        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.list)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope
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
                            translator.mapReferenceTypeIgnoringNullability(argument, swiftExportScope)
                        }
                    }

                    SwiftClassTypeModel("Array", typeArguments)
                }
            }


        }
    }

    private object SetMapper: CustomTypeMapper {
        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.set)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
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
                            translator.mapReferenceTypeIgnoringNullability(argument, swiftExportScope.addingFlags(SwiftExportScope.Flags.Hashable))
                        }
                    }

                    SwiftClassTypeModel("Set", typeArguments)
                }
            }
        }
    }

    private object MapMapper: CustomTypeMapper {
        override val mappedClassId: ClassId = ClassId.topLevel(StandardNames.FqNames.map)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
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
                            translator.mapReferenceTypeIgnoringNullability(argument, argumentScope)
                        }
                    }

                    SwiftClassTypeModel("Dictionary", typeArguments)
                }

            }
        }
    }

    private class Simple(
        override val mappedClassId: ClassId,
        private val getObjCClassName: SwiftTypeTranslator.() -> String
    ) : CustomTypeMapper {

        constructor(
            mappedClassId: ClassId,
            objCClassName: String
        ) : this(mappedClassId, { objCClassName })

        context(SwiftModelScope)
        override fun mapType(mappedSuperType: KotlinType, translator: SwiftTypeTranslator, swiftExportScope: SwiftExportScope): SwiftNonNullReferenceTypeModel =
            SwiftClassTypeModel(translator.getObjCClassName())
    }

    private class Collection(
        mappedClassFqName: FqName,
        private val getObjCClassName: SwiftTypeTranslator.() -> String
    ) : CustomTypeMapper {

        constructor(
            mappedClassFqName: FqName,
            objCClassName: String
        ) : this(mappedClassFqName, { objCClassName })

        override val mappedClassId = ClassId.topLevel(mappedClassFqName)

        context(SwiftModelScope)
        override fun mapType(
            mappedSuperType: KotlinType,
            translator: SwiftTypeTranslator,
            swiftExportScope: SwiftExportScope,
        ): SwiftNonNullReferenceTypeModel {
            val typeArguments = mappedSuperType.arguments.map {
                val argument = it.type
                if (TypeUtils.isNullableType(argument)) {
                    // Kotlin `null` keys and values are represented as `NSNull` singleton.
                    SwiftAnyObjectTypeModel
                } else {
                    translator.mapReferenceTypeIgnoringNullability(argument, swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType))
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
            swiftExportScope: SwiftExportScope
        ): SwiftNonNullReferenceTypeModel {
            return translator.mapFunctionTypeIgnoringNullability(mappedSuperType, swiftExportScope, returnsVoid = false)
        }
    }
}

class SwiftTypeTranslator(
    private val descriptorProvider: DescriptorProvider,
    val namer: ObjCExportNamer,
    private val problemCollector: SwiftTranslationProblemCollector,
) {

    context(SwiftModelScope)
    internal fun mapReturnType(returnBridge: MethodBridge.ReturnValue, method: FunctionDescriptor, swiftExportScope: SwiftExportScope): SwiftTypeModel {
        return when (returnBridge) {
            MethodBridge.ReturnValue.Suspend,
            MethodBridge.ReturnValue.Void -> SwiftVoidTypeModel
            MethodBridge.ReturnValue.HashCode -> SwiftPrimitiveTypeModel.NSUInteger
            is MethodBridge.ReturnValue.Mapped -> mapType(method.returnType!!, swiftExportScope, returnBridge.bridge)
            MethodBridge.ReturnValue.WithError.Success -> SwiftPrimitiveTypeModel.BOOL
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                val successReturnType = mapReturnType(returnBridge.successBridge, method, swiftExportScope)

                if (!returnBridge.successMayBeZero) {
                    check(successReturnType is SwiftNonNullReferenceTypeModel
                        || (successReturnType is SwiftPointerTypeModel && !successReturnType.nullable)) {
                        "Unexpected return type: $successReturnType in $method"
                    }
                }

                successReturnType.makeNullableIfReferenceOrPointer()
            }
            MethodBridge.ReturnValue.Instance.InitResult,
            MethodBridge.ReturnValue.Instance.FactoryResult -> SwiftInstanceTypeModel
        }
    }

    context(SwiftModelScope)
    internal fun mapReferenceType(kotlinType: KotlinType, swiftExportScope: SwiftExportScope): SwiftReferenceTypeModel =
        mapReferenceTypeIgnoringNullability(kotlinType, swiftExportScope).withNullabilityOf(kotlinType)

    private fun SwiftNonNullReferenceTypeModel.withNullabilityOf(kotlinType: KotlinType): SwiftReferenceTypeModel =
        if (kotlinType.binaryRepresentationIsNullable()) SwiftNullableRefefenceTypeModel(this) else this

    context(SwiftModelScope)
    internal fun mapReferenceTypeIgnoringNullability(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceTypeModel {
        class TypeMappingMatch(val type: KotlinType, val descriptor: ClassDescriptor, val mapper: CustomTypeMapper)

        val typeMappingMatches = (listOf(kotlinType) + kotlinType.supertypes()).mapNotNull { type ->
            (type.constructor.declarationDescriptor as? ClassDescriptor)?.let { descriptor ->
                CustomTypeMappers.getMapper(descriptor)?.let { mapper ->
                    TypeMappingMatch(type, descriptor, mapper)
                }
            }
        }

        val mostSpecificMatches = typeMappingMatches.filter { match ->
            typeMappingMatches.all { otherMatch ->
                otherMatch.descriptor == match.descriptor || !otherMatch.descriptor.isSubclassOf(match.descriptor)
            }
        }

        if (mostSpecificMatches.size > 1) {
            val types = mostSpecificMatches.map { it.type }
            val firstType = types[0]
            val secondType = types[1]

            problemCollector.reportWarning(
                "Exposed type '$kotlinType' is '$firstType' and '$secondType' at the same time. " +
                    "This most likely wouldn't work as expected.")
        }

        mostSpecificMatches.firstOrNull()?.let {
            return it.mapper.mapType(it.type, this, swiftExportScope)
        }

        if (kotlinType.isTypeParameter()) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> return SwiftAnyHashableTypeModel
                else -> {
                    val genericTypeUsage = swiftExportScope.genericScope.getGenericTypeUsage(TypeUtils.getTypeParameterDescriptorOrNull(kotlinType))
                    if (genericTypeUsage != null) {
                        return genericTypeUsage
                    }
                }
            }

        }

        val classDescriptor = kotlinType.getErasedTypeClass()

        if (KotlinBuiltIns.isAny(classDescriptor) || classDescriptor.classId in CustomTypeMappers.hiddenTypes || classDescriptor.isInlined()) {
            return idType(swiftExportScope)
        }

        if (classDescriptor.defaultType.isObjCObjectType()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(classDescriptor, swiftExportScope)
        }

        if (!descriptorProvider.isTransitivelyExposed(classDescriptor)) {
            return idType(swiftExportScope)
        }

        return if (classDescriptor.isInterface) {
            when {
                swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
                else -> {
                    val model = translateClassOrInterfaceName(classDescriptor, swiftExportScope)
                    if (model is KotlinTypeSwiftModel) {
                        SwiftKotlinTypeProtocolTypeModel(model)
                    } else {
                        SwiftProtocolTypeModel(model.stableFqName)
                    }
                }
            }
        } else {
            val typeParamScope = swiftExportScope.replacingFlags(SwiftExportScope.Flags.ReferenceType)
            val typeArgs = kotlinType.arguments.map { typeProjection ->
                if (typeProjection.isStarProjection) {
                    idType(typeParamScope)
                } else {
                    mapReferenceTypeIgnoringNullability(typeProjection.type, typeParamScope)
                }
            }

            val model = translateClassOrInterfaceName(classDescriptor, swiftExportScope)
            if (model is KotlinTypeSwiftModel) {
                SwiftKotlinTypeClassTypeModel(model, typeArgs)
            } else {
                SwiftClassTypeModel(model.stableFqName, typeArgs)
            }
        }
    }

    private tailrec fun mapObjCObjectReferenceTypeIgnoringNullability(
        descriptor: ClassDescriptor,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceTypeModel {
        if (descriptor.isObjCMetaClass()) return SwiftMetaClassTypeModel
        if (descriptor.isObjCProtocolClass()) return foreignClassType("Protocol")

        if (descriptor.isExternalObjCClass() || descriptor.isObjCForwardDeclaration()) {
            return if (descriptor.isInterface) {
                val name = descriptor.name.asString().removeSuffix("Protocol")
                foreignProtocolType(name)
            } else {
                val name = descriptor.name.asString()
                foreignClassType(name)
            }
        }

        if (descriptor.isKotlinObjCClass()) {
            return mapObjCObjectReferenceTypeIgnoringNullability(descriptor.getSuperClassOrAny(), swiftExportScope)
        }

        return idType(swiftExportScope)
    }

    private fun foreignProtocolType(name: String): SwiftProtocolTypeModel {
        // generator?.referenceProtocol(name)
        return SwiftProtocolTypeModel(name)
    }

    private fun foreignClassType(name: String): SwiftClassTypeModel {
        // generator?.referenceClass(ObjCClassForwardDeclaration(name))
        return SwiftClassTypeModel(name)
    }

    private fun idType(swiftExportScope: SwiftExportScope): SwiftNonNullReferenceTypeModel {
        return when {
            swiftExportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> SwiftAnyHashableTypeModel
            swiftExportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> SwiftAnyObjectTypeModel
            else -> SwiftAnyTypeModel
        }
    }

    context(SwiftModelScope)
    private fun mapFunctionTypeIgnoringNullability(
        functionType: KotlinType,
        swiftExportScope: SwiftExportScope,
        returnsVoid: Boolean,
    ): SwiftLambdaTypeModel {
        val parameterTypes = listOfNotNull(functionType.getReceiverTypeFromFunctionType()) +
            functionType.getValueParameterTypesFromFunctionType().map { it.type }

        return SwiftLambdaTypeModel(
            if (returnsVoid) {
                SwiftVoidTypeModel
            } else {
                mapReferenceType(functionType.getReturnTypeFromFunctionType(), swiftExportScope.removingFlags(SwiftExportScope.Flags.Escaping))
            },
            parameterTypes.map { mapReferenceType(it, swiftExportScope.addingFlags(SwiftExportScope.Flags.Escaping)) },
            isEscaping = swiftExportScope.hasFlag(SwiftExportScope.Flags.Escaping) && !functionType.binaryRepresentationIsNullable(),
        )
    }

    context(SwiftModelScope)
    private fun mapFunctionType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge.BlockPointer,
    ): SwiftReferenceTypeModel {
        val expectedDescriptor = kotlinType.builtIns.getFunction(typeBridge.numberOfParameters)

        val functionType = if (TypeUtils.getClassDescriptor(kotlinType) == expectedDescriptor) {
            kotlinType
        } else {
            kotlinType.supertypes().singleOrNull { TypeUtils.getClassDescriptor(it) == expectedDescriptor }
                ?: expectedDescriptor.defaultType // Should not happen though.
        }

        return mapFunctionTypeIgnoringNullability(functionType, swiftExportScope, typeBridge.returnsVoid)
            .withNullabilityOf(kotlinType)
    }

    context(SwiftModelScope)
    internal fun mapType(
        kotlinType: KotlinType,
        swiftExportScope: SwiftExportScope,
        typeBridge: NativeTypeBridge,
    ): SwiftTypeModel = when (typeBridge) {
        NativeTypeBridge.Reference -> mapReferenceType(kotlinType, swiftExportScope)
        is NativeTypeBridge.BlockPointer -> mapFunctionType(kotlinType, swiftExportScope, typeBridge)
        is NativeTypeBridge.ValueType -> when (typeBridge.objCValueType) {
            ObjCValueType.BOOL -> SwiftPrimitiveTypeModel.BOOL
            ObjCValueType.UNICHAR -> SwiftPrimitiveTypeModel.unichar
            ObjCValueType.CHAR -> SwiftPrimitiveTypeModel.int8_t
            ObjCValueType.SHORT -> SwiftPrimitiveTypeModel.int16_t
            ObjCValueType.INT -> SwiftPrimitiveTypeModel.int32_t
            ObjCValueType.LONG_LONG -> SwiftPrimitiveTypeModel.int64_t
            ObjCValueType.UNSIGNED_CHAR -> SwiftPrimitiveTypeModel.uint8_t
            ObjCValueType.UNSIGNED_SHORT -> SwiftPrimitiveTypeModel.uint16_t
            ObjCValueType.UNSIGNED_INT -> SwiftPrimitiveTypeModel.uint32_t
            ObjCValueType.UNSIGNED_LONG_LONG -> SwiftPrimitiveTypeModel.uint64_t
            ObjCValueType.FLOAT -> SwiftPrimitiveTypeModel.float
            ObjCValueType.DOUBLE -> SwiftPrimitiveTypeModel.double
            ObjCValueType.POINTER -> SwiftPointerTypeModel(SwiftVoidTypeModel, kotlinType.binaryRepresentationIsNullable())
        }
    }

    context(SwiftModelScope)
    private fun translateClassOrInterfaceName(descriptor: ClassDescriptor, exportScope: SwiftExportScope): TypeSwiftModel {
        assert(descriptorProvider.isTransitivelyExposed(descriptor)) { "Shouldn't be exposed: $descriptor" }

        if (ErrorUtils.isError(descriptor)) {
            return SwiftErrorTypeModel
        }
        val swiftModel = descriptor.swiftModel
        val bridge = swiftModel.bridge

        return when {
            exportScope.hasFlag(SwiftExportScope.Flags.ReferenceType) -> swiftModel
            exportScope.hasFlag(SwiftExportScope.Flags.Hashable) -> if (bridge is SwiftTypeSwiftModel && bridge.isHashable) {
                bridge
            } else {
                swiftModel
            }
            else -> swiftModel.bridge ?: swiftModel
        }
    }
}

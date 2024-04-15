@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.configuration.ValueParameterConfiguration
import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirScope
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.kir.type.translation.KirTypeParameterScope
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.kir.util.addOverrides
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.phases.DescriptorConversionPhase
import co.touchlab.skie.phases.oir.util.getOirValueParameterName
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.KonanFqNames
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.valueParametersAssociated
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ParameterDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.isValueClass

class CreateKirMembersPhase(
    context: DescriptorConversionPhase.Context,
) : DescriptorConversionPhase {

    private val descriptorProvider = context.descriptorProvider
    private val descriptorKirProvider = context.descriptorKirProvider
    private val kirProvider = context.kirProvider
    private val mapper = context.mapper
    private val descriptorConfigurationProvider = context.descriptorConfigurationProvider
    private val namer = context.namer
    private val kirDeclarationTypeTranslator = context.kirDeclarationTypeTranslator

    private val functionCache = mutableMapOf<FunctionDescriptor, KirSimpleFunction>()
    private val propertyCache = mutableMapOf<PropertyDescriptor, KirProperty>()

    private val convertedPropertyKindLazyInitializers = mutableListOf<() -> Unit>()

    context(DescriptorConversionPhase.Context)
    override suspend fun execute() {
        kirProvider.kotlinClasses.forEach(::createMembers)

        kirProvider.initializeCallableDeclarationsCache()

        initializeConvertedPropertyKinds()
    }

    private fun initializeConvertedPropertyKinds() {
        convertedPropertyKindLazyInitializers.forEach { it() }
    }

    private fun createMembers(kirClass: KirClass) {
        if (kirClass in kirProvider.kirBuiltins.builtinClasses) {
            // TODO Implement accurate way to generate builtin members once needed
            return
        }

        when (kirClass.kind) {
            KirClass.Kind.File -> createMembers(descriptorKirProvider.getClassSourceFile(kirClass), kirClass)
            else -> createMembers(descriptorKirProvider.getClassDescriptor(kirClass), kirClass)
        }
    }

    private fun createMembers(classDescriptor: ClassDescriptor, kirClass: KirClass) {
        descriptorProvider.getExposedConstructors(classDescriptor).forEach {
            createConstructor(it, kirClass)
        }

        descriptorProvider.getExposedClassMembers(classDescriptor).forEach {
            createMember(it, kirClass, Origin.Member)
        }

        descriptorProvider.getExposedCategoryMembers(classDescriptor).forEach {
            createMember(it, kirClass, Origin.Extension)
        }
    }

    private fun createMembers(sourceFile: SourceFile, kirClass: KirClass) {
        descriptorProvider.getExposedStaticMembers(sourceFile).forEach {
            val scope = if (it.extensionReceiverParameter != null) Origin.Extension else Origin.Global

            createMember(it, kirClass, scope)
        }
    }

    private fun createConstructor(descriptor: ConstructorDescriptor, kirClass: KirClass) {
        val originalDescriptor = descriptor.original

        val methodBridge = mapper.bridgeMethod(originalDescriptor)

        val constructor = KirConstructor(
            kotlinName = descriptor.name.asString(),
            kotlinSignature = descriptor.signature,
            objCSelector = namer.getSelector(descriptor),
            swiftName = namer.getSwiftName(descriptor),
            owner = kirClass,
            errorHandlingStrategy = methodBridge.returnBridge.errorHandlingStrategy,
            deprecationLevel = descriptor.kirDeprecationLevel,
            configuration = descriptorConfigurationProvider.getConfiguration(descriptor),
        )

        descriptorKirProvider.registerCallableDeclaration(constructor, descriptor)

        kirClass.withTypeParameterScope {
            createValueParameters(constructor, originalDescriptor, methodBridge)
        }
    }

    private fun createMember(descriptor: CallableMemberDescriptor, kirClass: KirClass, origin: Origin) {
        when (descriptor) {
            is SimpleFunctionDescriptor -> getOrCreateFunction(descriptor, kirClass, origin)
            is PropertyDescriptor -> {
                if (mapper.isObjCProperty(descriptor.baseProperty)) {
                    getOrCreateProperty(descriptor, kirClass, origin)
                } else {
                    descriptor.getter?.let { getOrCreateFunction(it, kirClass, origin) }
                    descriptor.setter?.let { getOrCreateFunction(it, kirClass, origin) }
                }
            }
            else -> error("Unsupported member: $descriptor")
        }
    }

    private fun getOrCreateFunction(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirSimpleFunction =
        functionCache.getOrPut(descriptor.original) {
            createFunction(descriptor, kirClass, origin)
        }

    private fun getOrCreateOverriddenFunction(descriptor: FunctionDescriptor, origin: Origin): KirSimpleFunction {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported function $descriptor")

        val kirClass = descriptorKirProvider.getClass(classDescriptor)

        return getOrCreateFunction(descriptor, kirClass, origin)
    }

    private fun createFunction(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirSimpleFunction {
        val baseDescriptor = descriptor.baseFunction
        val originalDescriptor = descriptor.original

        val methodBridge = mapper.bridgeMethod(baseDescriptor)

        kirClass.withTypeParameterScope {
            val function = KirSimpleFunction(
                kotlinName = descriptor.name.asString(),
                kotlinSignature = descriptor.signature,
                objCSelector = namer.getSelector(baseDescriptor),
                swiftName = namer.getSwiftName(baseDescriptor),
                owner = kirClass,
                origin = origin,
                isFakeOverride = if (kirClass == kirProvider.kirBuiltins.Base) {
                    // TODO Solves issue with methods from Any which are technically a fake override - remove once the builtins have correct members
                    true
                } else {
                    !descriptor.kind.isReal
                },
                isSuspend = descriptor.isSuspend,
                kind = descriptor.getKind(kirClass, origin),
                returnType = kirDeclarationTypeTranslator.mapReturnType(
                    originalDescriptor,
                    methodBridge.returnBridge,
                ),
                scope = kirClass.callableDeclarationScope,
                errorHandlingStrategy = methodBridge.returnBridge.errorHandlingStrategy,
                deprecationLevel = descriptor.kirDeprecationLevel,
                isRefinedInSwift = baseDescriptor.isRefinedInSwift,
                configuration = getFunctionConfiguration(descriptor),
            )

            descriptorKirProvider.registerCallableDeclaration(function, descriptor)

            getDirectParents(descriptor)
                .map { getOrCreateOverriddenFunction(it, origin) }
                .let { function.addOverrides(it) }

            createValueParameters(function, descriptor, methodBridge)

            return function
        }
    }

    private fun FunctionDescriptor.getKind(kirClass: KirClass, origin: Origin): KirSimpleFunction.Kind =
        when (this) {
            is SimpleFunctionDescriptor -> KirSimpleFunction.Kind.Function
            is PropertyGetterDescriptor -> {
                val kind = KirSimpleFunction.Kind.PropertyGetter(null)

                this.correspondingProperty.setter?.let {
                    convertedPropertyKindLazyInitializers.add {
                        kind.associatedSetter = getOrCreateFunction(it, kirClass, origin)
                    }
                }

                kind
            }
            is PropertySetterDescriptor -> {
                val kind = KirSimpleFunction.Kind.PropertySetter(null)

                this.correspondingProperty.getter?.let {
                    convertedPropertyKindLazyInitializers.add {
                        kind.associatedGetter = getOrCreateFunction(it, kirClass, origin)
                    }
                }

                kind
            }
            else -> error("Unsupported function type: $this")
        }

    private fun getFunctionConfiguration(descriptor: FunctionDescriptor): SimpleFunctionConfiguration =
        when (descriptor) {
            is SimpleFunctionDescriptor -> descriptorConfigurationProvider.getConfiguration(descriptor)
            is PropertyAccessorDescriptor -> {
                val propertyConfiguration = descriptorConfigurationProvider.getConfiguration(descriptor.correspondingProperty)

                val functionConfiguration = SimpleFunctionConfiguration(propertyConfiguration.parent)

                functionConfiguration.overwriteBy(propertyConfiguration)

                functionConfiguration
            }
            else -> error("Unsupported function type: $descriptor")
        }

    private fun getOrCreateProperty(
        descriptor: PropertyDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirProperty =
        propertyCache.getOrPut(descriptor.original) {
            createProperty(descriptor, kirClass, origin)
        }

    private fun getOrCreateOverriddenProperty(descriptor: PropertyDescriptor, origin: Origin): KirProperty {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported property $descriptor")

        val kirClass = descriptorKirProvider.getClass(classDescriptor)

        return getOrCreateProperty(descriptor, kirClass, origin)
    }

    private fun createProperty(
        descriptor: PropertyDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirProperty {
        val baseDescriptor = descriptor.baseProperty
        val originalDescriptor = descriptor.original

        val getterBridge = mapper.bridgeMethod(baseDescriptor.getter!!)

        val propertyName = namer.getPropertyName(baseDescriptor)

        kirClass.withTypeParameterScope {
            val property = KirProperty(
                kotlinName = descriptor.name.asString(),
                kotlinSignature = descriptor.signature,
                objCName = propertyName.objCName,
                swiftName = propertyName.swiftName,
                owner = kirClass,
                origin = origin,
                scope = kirClass.callableDeclarationScope,
                isFakeOverride = !descriptor.kind.isReal,
                type = kirDeclarationTypeTranslator.mapReturnType(originalDescriptor.getter!!, getterBridge.returnBridge),
                isVar = descriptor.setter?.let { mapper.shouldBeExposed(it) } ?: false,
                deprecationLevel = descriptor.kirDeprecationLevel,
                isRefinedInSwift = baseDescriptor.isRefinedInSwift,
                configuration = descriptorConfigurationProvider.getConfiguration(originalDescriptor),
            )

            descriptorKirProvider.registerCallableDeclaration(property, descriptor)

            getDirectParents(descriptor)
                .map { getOrCreateOverriddenProperty(it, origin) }
                .let { property.addOverrides(it) }

            return property
        }
    }

    context(KirTypeParameterScope)
    private fun createValueParameters(
        function: KirFunction<*>,
        descriptor: FunctionDescriptor,
        methodBridge: MethodBridge,
    ) {
        methodBridge.valueParametersAssociated(descriptor)
            .forEach { (parameterBridge, parameterDescriptor) ->
                createValueParameter(parameterBridge, parameterDescriptor, function, descriptor)
            }
    }

    context(KirTypeParameterScope)
    private fun createValueParameter(
        parameterBridge: MethodBridgeValueParameter,
        parameterDescriptor: ParameterDescriptor?,
        function: KirFunction<*>,
        functionDescriptor: FunctionDescriptor,
    ) {
        val kind = when (parameterBridge) {
            is MethodBridgeValueParameter.Mapped -> when (parameterDescriptor) {
                null -> error("Mapped ValueParameter $parameterBridge has no descriptor.")
                is ReceiverParameterDescriptor -> KirValueParameter.Kind.Receiver
                is PropertySetterDescriptor -> KirValueParameter.Kind.PropertySetterValue
                else -> KirValueParameter.Kind.ValueParameter
            }
            MethodBridgeValueParameter.ErrorOutParameter -> KirValueParameter.Kind.ErrorOut
            is MethodBridgeValueParameter.SuspendCompletion -> KirValueParameter.Kind.SuspendCompletion
        }

        val kotlinName = when (kind) {
            is KirValueParameter.Kind.ValueParameter -> parameterDescriptor!!.name.asString()
            KirValueParameter.Kind.Receiver -> "receiver"
            KirValueParameter.Kind.PropertySetterValue -> "value"
            KirValueParameter.Kind.ErrorOut -> "error"
            KirValueParameter.Kind.SuspendCompletion -> "completionHandler"
        }

        val valueParameter = KirValueParameter(
            kotlinName = kotlinName,
            objCName = when (kind) {
                is KirValueParameter.Kind.ValueParameter -> namer.getOirValueParameterName(parameterDescriptor!!)
                else -> kotlinName
            }.toValidSwiftIdentifier(),
            parent = function,
            type = kirDeclarationTypeTranslator.mapValueParameterType(functionDescriptor, parameterDescriptor, parameterBridge),
            kind = kind,
            configuration = getValueParameterConfiguration(parameterDescriptor, function),
            wasTypeInlined = parameterDescriptor?.type?.constructor?.declarationDescriptor?.isValueClass() == true,
        )

        parameterDescriptor?.let {
            descriptorKirProvider.registerValueParameter(valueParameter, parameterDescriptor)
        }
    }

    private fun getValueParameterConfiguration(
        parameterDescriptor: ParameterDescriptor?,
        function: KirFunction<*>,
    ): ValueParameterConfiguration =
        if (parameterDescriptor != null) {
            descriptorConfigurationProvider.getConfiguration(parameterDescriptor)
        } else {
            ValueParameterConfiguration(function.configuration)
        }

    private val FunctionDescriptor.baseFunction: FunctionDescriptor
        get() = (getAllParents(this) + this.original).first { mapper.isBaseMethod(it) }

    private fun getAllParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getDirectParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { mapper.shouldBeExposed(it) }

    private val PropertyDescriptor.baseProperty: PropertyDescriptor
        get() = (getAllParents(this) + this.original).first { mapper.isBaseProperty(it) }

    private fun getAllParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getDirectParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { mapper.shouldBeExposed(it) }

    private val KirClass.callableDeclarationScope: KirScope
        get() = when (this.kind) {
            KirClass.Kind.File -> KirScope.Static
            else -> KirScope.Member
        }

    private val CallableMemberDescriptor.kirDeprecationLevel: DeprecationLevel
        get() {
            val deprecationInfo = mapper.getDeprecation(this)

            return when (deprecationInfo?.deprecationLevel) {
                DeprecationLevelValue.ERROR -> DeprecationLevel.Error(deprecationInfo.message)
                DeprecationLevelValue.WARNING -> DeprecationLevel.Warning(deprecationInfo.message)
                DeprecationLevelValue.HIDDEN -> DeprecationLevel.Error(deprecationInfo.message)
                null -> DeprecationLevel.None
            }
        }

    private val CallableMemberDescriptor.isRefinedInSwift: Boolean
        get() = annotations.any { annotation ->
            annotation.annotationClass?.annotations?.any { it.fqName == KonanFqNames.refinesInSwift } == true
        }

    private val CallableMemberDescriptor.signature: String
        get() = with(KonanManglerDesc) {
            this@signature.signatureString(false)
        }

    private val MethodBridge.ReturnValue.errorHandlingStrategy: OirFunction.ErrorHandlingStrategy
        get() = when (this) {
            MethodBridge.ReturnValue.WithError.Success -> OirFunction.ErrorHandlingStrategy.ReturnsBoolean
            is MethodBridge.ReturnValue.WithError.ZeroForError -> {
                if (this.successMayBeZero) {
                    OirFunction.ErrorHandlingStrategy.SetsErrorOut
                } else {
                    OirFunction.ErrorHandlingStrategy.ReturnsZero
                }
            }
            else -> OirFunction.ErrorHandlingStrategy.Crashes
        }
}

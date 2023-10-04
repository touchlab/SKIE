@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.sir

import co.touchlab.skie.compilerinject.reflection.reflectors.mapper
import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirGetter
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSetter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirValueParameterParent
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.addOverrides
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.DescriptorBridgeProvider
import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftGenericExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModelWithCore
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridge
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridgeParameter
import co.touchlab.skie.swiftmodel.type.bridge.valueParametersAssociated
import co.touchlab.skie.swiftmodel.type.translation.SwiftTypeTranslator
import co.touchlab.skie.util.collisionFreeIdentifier
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.doesThrow
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny

// WIP 2 Refactor and optimize multiple calls to the same translation functions
class KotlinSirCallableDeclarationsFactory(
    private val sirProvider: SirProvider,
    private val translator: SwiftTypeTranslator,
    private val namer: ObjCExportNamer,
    private val descriptorProvider: DescriptorProvider,
    private val swiftModelScope: SwiftModelScope,
    private val bridgeProvider: DescriptorBridgeProvider,
    private val kotlinSirClassFactory: KotlinSirClassFactory,
    private val skieContext: SkiePhase.Context,
) {

    private val kotlinSirConstructorCache = mutableMapOf<FunctionDescriptor, SirConstructor>()

    private val kotlinSirFunctionCache = mutableMapOf<FunctionDescriptor, SirFunction>()

    private val kotlinSirAsyncFunctionCache = mutableMapOf<FunctionDescriptor, SirFunction>()

    private val kotlinSirPropertyCache = mutableMapOf<PropertyDescriptor, SirProperty>()

    val sirBuiltins: SirBuiltins
        get() = sirProvider.sirBuiltins

    fun getKotlinSirFunctionOrConstructor(functionDescriptor: FunctionDescriptor): SirCallableDeclaration =
        if (functionDescriptor is ConstructorDescriptor) {
            getKotlinSirConstructor(functionDescriptor)
        } else {
            getKotlinSirFunction(functionDescriptor)
        }

    fun getKotlinSirConstructor(functionDescriptor: FunctionDescriptor): SirConstructor =
        kotlinSirConstructorCache.getOrPut(functionDescriptor) {
            SirConstructor(
                parent = functionDescriptor.sirParent as SirClass,
            ).apply {
                addKotlinValueParameters(functionDescriptor, keepAsyncCallback = false)
            }
        }

    fun getKotlinSirFunction(functionDescriptor: FunctionDescriptor): SirFunction =
        kotlinSirFunctionCache.getOrPut(functionDescriptor) {
            SirFunction(
                identifier = functionDescriptor.swiftFunctionName.identifier,
                parent = functionDescriptor.sirParent,
                returnType = functionDescriptor.regularReturnType,
                scope = functionDescriptor.sirScope,
                throws = namer.mapper.doesThrow(functionDescriptor),
            ).apply {
                getDirectParents(functionDescriptor)
                    .map { getKotlinSirFunction(it) }
                    .let { addOverrides(it) }

                addKotlinValueParameters(functionDescriptor, keepAsyncCallback = true)
            }
        }

    private val CallableMemberDescriptor.sirScope: SirScope
        get() = if (descriptorProvider.getReceiverClassDescriptorOrNull(this) == null) {
            SirScope.Static
        } else {
            SirScope.Member
        }

    fun getKotlinSirAsyncFunction(functionDescriptor: FunctionDescriptor): SirFunction =
        kotlinSirAsyncFunctionCache.getOrPut(functionDescriptor) {
            require(functionDescriptor.isSuspend) { "Function $functionDescriptor is not suspend." }

            val regularFunction = getKotlinSirFunctionOrConstructor(functionDescriptor)

            SirFunction(
                identifier = regularFunction.identifier,
                parent = regularFunction.parent,
                returnType = functionDescriptor.asyncReturnType,
                scope = regularFunction.scope,
                attributes = regularFunction.attributes,
                isAsync = true,
                throws = true,
            ).apply {
                getDirectParents(functionDescriptor)
                    .map { getKotlinSirAsyncFunction(it) }
                    .let { addOverrides(it) }

                addAvailabilityForAsync()

                addKotlinValueParameters(functionDescriptor, keepAsyncCallback = false)
            }
        }

    fun getKotlinSirProperty(propertyDescriptor: PropertyDescriptor): SirProperty =
        kotlinSirPropertyCache.getOrPut(propertyDescriptor) {
            SirProperty(
                identifier = namer.getPropertyName(propertyDescriptor.baseProperty).swiftName,
                parent = propertyDescriptor.sirParent,
                type = propertyDescriptor.propertyType,
                scope = propertyDescriptor.sirScope,
            ).apply {
                getDirectParents(propertyDescriptor)
                    .map { getKotlinSirProperty(it) }
                    .let { addOverrides(it) }

                propertyDescriptor.getter?.let {
                    SirGetter(
                        throws = namer.mapper.doesThrow(it),
                    )
                }
                propertyDescriptor.setter?.let {
                    SirSetter(
                        throws = namer.mapper.doesThrow(it),
                    )
                }
            }
        }

    fun createKotlinSirFakeObjCConstructor(
        classDescriptor: ClassDescriptor,
        representativeModel: KotlinFunctionSwiftModelWithCore,
    ): SirConstructor =
        SirConstructor(
            parent = sirProvider.getKotlinSirClass(classDescriptor),
            visibility = SirVisibility.Removed,
        ).apply {
            representativeModel.kotlinSirConstructor.valueParameters.forEach {
                SirValueParameter(
                    label = it.label,
                    name = it.name,
                    type = it.type,
                )
            }
        }

    @Suppress("RecursivePropertyAccessor")
    private val CallableMemberDescriptor.sirParent: SirDeclarationParent
        get() {
            val receiverClassDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(this)
            val containingDeclaration = containingDeclaration

            return when {
                receiverClassDescriptor != null -> sirProvider.getKotlinSirClass(receiverClassDescriptor)
                this is PropertyAccessorDescriptor -> correspondingProperty.sirParent
                containingDeclaration is PackageFragmentDescriptor -> sirProvider.getKotlinSirClass(this.findSourceFile())
                else -> error("Unsupported containing declaration for $this")
            }
        }

    private val FunctionDescriptor.regularReturnType: SirType
        get() {
            val exportScope = SwiftExportScope(sirParent.swiftGenericExportScope)

            return with(swiftModelScope) {
                with(skieContext) {
                    translator.mapReturnType(bridge.returnBridge, this@regularReturnType, exportScope, flowMappingStrategy)
                }
            }
        }

    private val FunctionDescriptor.asyncReturnType: SirType
        get() {
            val parameterBridge = bridge.paramBridges.firstNotNullOf { it as? MethodBridgeParameter.ValueParameter.SuspendCompletion }

            val exportScope = SwiftExportScope(sirParent.swiftGenericExportScope)
            return if (parameterBridge.useUnitCompletion) {
                sirBuiltins.Swift.Void.defaultType
            } else {
                with(swiftModelScope) {
                    with(skieContext) {
                        translator.mapReferenceType(returnType!!, exportScope, flowMappingStrategy)
                    }
                }
            }
        }

    private val PropertyDescriptor.propertyType: SirType
        get() {
            val getterBridge = bridgeProvider.bridgeMethod(this.getter?.baseFunction!!)

            val exportScope = SwiftExportScope(sirParent.swiftGenericExportScope)

            return with(swiftModelScope) {
                with(skieContext) {
                    translator.mapReturnType(getterBridge.returnBridge, getter!!, exportScope, flowMappingStrategy)
                }
            }
        }

    // WIP 2 Might be needed elsewhere
    @Suppress("RecursivePropertyAccessor")
    private val SirDeclarationParent.swiftGenericExportScope: SwiftGenericExportScope
        get() = when (this) {
            is SirClass -> kotlinSirClassFactory.getClassDescriptorForKotlinSirClass(this)
                ?.let { SwiftGenericExportScope.Class(it, typeParameters) }
                ?: SwiftGenericExportScope.None
            is SirExtension -> classDeclaration.swiftGenericExportScope
            else -> SwiftGenericExportScope.None
        }

    // WIP 2 Test if this logic around based methods and constructors cannot be simplified
    private val FunctionDescriptor.bridge: MethodBridge
        get() = if (this is ConstructorDescriptor) {
            bridgeProvider.bridgeMethod(this)
        } else {
            bridgeProvider.bridgeMethod(baseFunction)
        }

    private val FunctionDescriptor.baseFunction: FunctionDescriptor
        get() = (getAllParents(this) + this.original).first { namer.mapper.isBaseMethod(it) }

    private val PropertyDescriptor.baseProperty: PropertyDescriptor
        get() = (getAllParents(this) + this.original).first { namer.mapper.isBaseProperty(it) }

    private fun getAllParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getAllParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getDirectParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        if (descriptor is ConstructorDescriptor) getDirectParents(descriptor) else descriptor.overriddenDescriptors.map { it.original }
            .filter { namer.mapper.shouldBeExposed(it) }

    private fun getDirectParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }.filter { namer.mapper.shouldBeExposed(it) }

    private fun getDirectParents(descriptor: ConstructorDescriptor): List<ConstructorDescriptor> =
        descriptor.constructedClass
            .getAllSuperclassesWithoutAny()
            .flatMap { it.constructors }
            .filter { namer.getSelector(it) == namer.getSelector(descriptor) }

    private fun SirValueParameterParent.addKotlinValueParameters(functionDescriptor: FunctionDescriptor, keepAsyncCallback: Boolean) {
        val swiftFunctionName = functionDescriptor.swiftFunctionName

        functionDescriptor.bridge
            .valueParametersAssociated(functionDescriptor)
            .filterNot { it.first is MethodBridgeParameter.ValueParameter.ErrorOutParameter }
            .filter { keepAsyncCallback || it.first !is MethodBridgeParameter.ValueParameter.SuspendCompletion }
            .zip(swiftFunctionName.argumentLabels)
            .forEach { (parameterBridgeWithDescriptor, argumentLabel) ->
                val (parameterBridge, parameterDescriptor) = parameterBridgeWithDescriptor

                SirValueParameter(
                    label = argumentLabel,
                    //    Makes certain assumptions about the inner workings of method bridging.
                    //        - Assumes that each function has at most one explicit receiver parameter
                    //        - And that label of that parameter is "_"
                    //    As a result it's safe for now to use argumentLabels as existingNames
                    name = when (parameterBridge) {
                        is MethodBridgeParameter.ValueParameter.Mapped -> {
                            when (parameterDescriptor) {
                                is PropertySetterDescriptor -> "value"
                                is ValueParameterDescriptor -> parameterDescriptor.name.asString().toValidSwiftIdentifier()
                                is ReceiverParameterDescriptor -> parameterDescriptor.name.asStringStripSpecialMarkers()
                                    .toValidSwiftIdentifier().collisionFreeIdentifier(swiftFunctionName.argumentLabels)
                                null -> error("Mapped parameter does not have a descriptor: $parameterBridge")
                                else -> error("Unknown parameter descriptor type: $parameterDescriptor")
                            }
                        }
                        is MethodBridgeParameter.ValueParameter.SuspendCompletion -> "completionHandler"
                        MethodBridgeParameter.ValueParameter.ErrorOutParameter -> error("This type should be filtered out above.")
                    },
                    type = with(swiftModelScope) {
                        with(skieContext) {
                            functionDescriptor.getParameterType(
                                parameterDescriptor,
                                parameterBridge,
                                parent.swiftGenericExportScope,
                                functionDescriptor.flowMappingStrategy,
                            )
                        }
                    },
                )
            }
    }

    private val FunctionDescriptor.swiftFunctionName: SwiftFunctionName
        get() {
            val swiftName = namer.getSwiftName(this.baseFunction)

            val (identifier, argumentLabelsString) = swiftNameComponentsRegex.matchEntire(swiftName)?.destructured
                ?: error("Unable to parse swift name: $swiftName")

            val argumentLabels = argumentLabelsString.split(":").map { it.trim() }.filter { it.isNotEmpty() }

            return SwiftFunctionName(identifier, argumentLabels)
        }

    private data class SwiftFunctionName(val identifier: String, val argumentLabels: List<String>)

    private companion object {

        val swiftNameComponentsRegex = "(.+?)\\((.*?)\\)".toRegex()
    }
}

// WIP 2 Replace other usages and instead copy attributes from wrapped function, but keep public because it might be needed in the future if we generate functions from scratch
fun SirFunction.addAvailabilityForAsync() {
    attributes.add("available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)")
}

// WIP 2 refactor
// WIP 2 This is probably wrong, the configuration has to be the same for the whole hierarchy
context(SkiePhase.Context)
val CallableMemberDescriptor.flowMappingStrategy: FlowMappingStrategy
    get() = if (this.isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None

context(SkiePhase.Context)
private val CallableMemberDescriptor.isFlowInteropEnabled: Boolean
    get() = SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags &&
        configurationProvider.getConfiguration(this, FlowInterop.Enabled)

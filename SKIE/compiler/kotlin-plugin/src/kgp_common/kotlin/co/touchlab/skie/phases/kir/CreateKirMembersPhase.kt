@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.compilerinject.reflection.reflectors.mapper
import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirScope
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.kir.util.addOverrides
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridge
import org.jetbrains.kotlin.backend.konan.objcexport.MethodBridgeValueParameter
import org.jetbrains.kotlin.backend.konan.objcexport.getDeprecation
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.valueParametersAssociated
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue

class CreateKirMembersPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val descriptorProvider = context.descriptorProvider
    private val kirProvider = context.kirProvider
    private val mapper = context.namer.mapper
    private val kirTypeTranslator = context.kirTypeTranslator

    private val functionCache = mutableMapOf<FunctionDescriptor, KirSimpleFunction>()
    private val propertyCache = mutableMapOf<PropertyDescriptor, KirProperty>()

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allClasses.forEach(::createMembers)

        kirProvider.initializeCallableDeclarationsCache()
    }

    private fun createMembers(kirClass: KirClass) {
        when (kirClass.descriptor) {
            is KirClass.Descriptor.Class -> createMembers(kirClass.descriptor.value, kirClass)
            is KirClass.Descriptor.File -> createMembers(kirClass.descriptor.value, kirClass)
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
            descriptor = originalDescriptor,
            owner = kirClass,
            errorHandlingStrategy = methodBridge.returnBridge.errorHandlingStrategy,
            deprecationLevel = descriptor.kirDeprecationLevel,
        )

        createValueParameters(constructor, originalDescriptor, methodBridge)
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

    private fun getOrCreateOverridenFunction(descriptor: FunctionDescriptor, origin: Origin): KirSimpleFunction {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported function $descriptor")

        val kirClass = kirProvider.getClass(classDescriptor)

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

        val function = KirSimpleFunction(
            baseDescriptor = baseDescriptor,
            descriptor = originalDescriptor,
            owner = kirClass,
            origin = origin,
            isSuspend = descriptor.isSuspend,
            returnType = kirTypeTranslator.mapReturnType(originalDescriptor, methodBridge.returnBridge),
            kind = when (descriptor) {
                is SimpleFunctionDescriptor -> KirSimpleFunction.Kind.Function
                is PropertyGetterDescriptor -> KirSimpleFunction.Kind.PropertyGetter(descriptor.correspondingProperty.original)
                is PropertySetterDescriptor -> KirSimpleFunction.Kind.PropertySetter(descriptor.correspondingProperty.original)
                else -> error("Unsupported function type: $descriptor")
            },
            scope = kirClass.callableDeclarationScope,
            errorHandlingStrategy = methodBridge.returnBridge.errorHandlingStrategy,
            deprecationLevel = descriptor.kirDeprecationLevel,
        )

        getDirectParents(descriptor)
            .map { getOrCreateOverridenFunction(it, origin) }
            .let { function.addOverrides(it) }

        createValueParameters(function, descriptor, methodBridge)

        return function
    }

    private fun getOrCreateProperty(
        descriptor: PropertyDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirProperty =
        propertyCache.getOrPut(descriptor.original) {
            createProperty(descriptor, kirClass, origin)
        }

    private fun getOrCreateOverridenProperty(descriptor: PropertyDescriptor, origin: Origin): KirProperty {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported property $descriptor")

        val kirClass = kirProvider.getClass(classDescriptor)

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

        val property = KirProperty(
            baseDescriptor = baseDescriptor,
            descriptor = originalDescriptor,
            owner = kirClass,
            origin = origin,
            scope = kirClass.callableDeclarationScope,
            type = kirTypeTranslator.mapReturnType(originalDescriptor.getter!!, getterBridge.returnBridge),
            isVar = descriptor.isVar,
            deprecationLevel = descriptor.kirDeprecationLevel,
        )

        getDirectParents(descriptor)
            .map { getOrCreateOverridenProperty(it, origin) }
            .let { property.addOverrides(it) }

        return property
    }

    private fun createValueParameters(
        function: KirFunction<*>,
        descriptor: FunctionDescriptor,
        methodBridge: MethodBridge,
    ) {
        methodBridge.valueParametersAssociated(descriptor)
            .forEach { (parameterBridge, parameterDescriptor) ->
                KirValueParameter(
                    parent = function,
                    type = kirTypeTranslator.mapValueParameterType(descriptor, parameterDescriptor, parameterBridge),
                    kind = when (parameterBridge) {
                        is MethodBridgeValueParameter.Mapped -> when (parameterDescriptor) {
                            null -> error("Mapped ValueParameter $parameterBridge has no descriptor.")
                            is ReceiverParameterDescriptor -> KirValueParameter.Kind.Receiver
                            is PropertySetterDescriptor -> KirValueParameter.Kind.PropertySetterValue
                            else -> KirValueParameter.Kind.ValueParameter(parameterDescriptor)
                        }
                        MethodBridgeValueParameter.ErrorOutParameter -> KirValueParameter.Kind.ErrorOut
                        is MethodBridgeValueParameter.SuspendCompletion -> KirValueParameter.Kind.SuspendCompletion
                    },
                )
            }
    }

    private val FunctionDescriptor.baseFunction: FunctionDescriptor
        get() = (getAllParents(this) + this.original).first { descriptorProvider.isBaseMethod(it) }

    private fun getAllParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getDirectParents(descriptor: FunctionDescriptor): List<FunctionDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { descriptorProvider.isExposable(it) }

    private val PropertyDescriptor.baseProperty: PropertyDescriptor
        get() = (getAllParents(this) + this.original).first { descriptorProvider.isBaseProperty(it) }

    private fun getAllParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        getDirectParents(descriptor).flatMap { getAllParents(it) + it.original }

    private fun getDirectParents(descriptor: PropertyDescriptor): List<PropertyDescriptor> =
        descriptor.overriddenDescriptors.map { it.original }
            .filter { descriptorProvider.isExposable(it) }

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

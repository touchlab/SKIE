@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.phases.kir

import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirCallableDeclaration.Origin
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.type.translation.withTypeParameterScope
import co.touchlab.skie.kir.util.addOverrides
import co.touchlab.skie.phases.CompilerDependentKirPhase
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class CreateKirSimpleFunctionsPhase(
    context: CompilerDependentKirPhase.Context,
) : BaseCreateRegularKirFunctionPhase(context, supportsSimpleFunctions = true) {

    private val functionCache = mutableMapOf<FunctionDescriptor, KirSimpleFunction>()

    private val convertedPropertyKindLazyInitializers = mutableListOf<() -> Unit>()

    private val needsDescriptionAndHashFunctions = SkieConfigurationFlag.Migration_AnyMethodsAsFunctions in context.rootConfiguration.enabledFlags

    context(CompilerDependentKirPhase.Context)
    override suspend fun execute() {
        super.execute()

        initializeConvertedPropertyKinds()
    }

    private fun initializeConvertedPropertyKinds() {
        convertedPropertyKindLazyInitializers.forEach { it() }
    }

    override fun visitFunction(descriptor: FunctionDescriptor, kirClass: KirClass, origin: Origin) {
        if (CreateKirDescriptionAndHashPropertyPhase.isToStringOrEquals(descriptor) && !needsDescriptionAndHashFunctions) {
            return
        }

        getOrCreateFunction(descriptor, kirClass, origin)
    }

    private fun getOrCreateFunction(
        descriptor: FunctionDescriptor,
        kirClass: KirClass,
        origin: Origin,
    ): KirSimpleFunction =
        functionCache.getOrPut(descriptor.original) {
            createFunction(descriptor, kirClass, origin)
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

    private fun getOrCreateOverriddenFunction(descriptor: FunctionDescriptor, origin: Origin): KirSimpleFunction {
        val classDescriptor = descriptorProvider.getReceiverClassDescriptorOrNull(descriptor)
            ?: error("Unsupported function $descriptor")

        val kirClass = descriptorKirProvider.getClass(classDescriptor)

        return getOrCreateFunction(descriptor, kirClass, origin)
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
}

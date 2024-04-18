package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.element.KirScope
import co.touchlab.skie.kir.irbuilder.createFunction
import co.touchlab.skie.kir.irbuilder.util.copyIndexing
import co.touchlab.skie.kir.irbuilder.util.copyWithoutDefaultValue
import co.touchlab.skie.kir.irbuilder.util.createValueParameter
import co.touchlab.skie.phases.FrontendIrPhase
import co.touchlab.skie.phases.KirPhase
import co.touchlab.skie.phases.declarationBuilder
import co.touchlab.skie.phases.descriptorKirProvider
import co.touchlab.skie.phases.descriptorProvider
import co.touchlab.skie.phases.features.suspend.kotlin.SuspendKotlinBridgeBodyGenerator
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.applyToEntireOverrideHierarchy
import co.touchlab.skie.util.collisionFreeIdentifier
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptorWithTypeParameters
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.TypeConstructorSubstitution
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.Variance

class KotlinSuspendGeneratorDelegate(
    private val context: FrontendIrPhase.Context,
) {

    private var nextBridgingFunctionIndex = 0

    private val suspendHandlerDescriptor: ClassDescriptor =
        context.descriptorProvider.exposedClasses
            .single { it.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.suspend.Skie_SuspendHandler") }

    private val bodyGenerator = SuspendKotlinBridgeBodyGenerator(suspendHandlerDescriptor)

    context(FrontendIrPhase.Context)
    fun generateKotlinBridgingFunction(functionDescriptor: FunctionDescriptor): FunctionDescriptor {
        val bridgingFunctionDescriptor = createBridgingFunction(functionDescriptor)

        bridgingFunctionDescriptor.hide()
        bridgingFunctionDescriptor.changeSkieConfiguration(functionDescriptor)

        return bridgingFunctionDescriptor
    }

    context(FrontendIrPhase.Context)
    private fun FunctionDescriptor.changeSkieConfiguration(originalFunctionDescriptor: FunctionDescriptor) {
        this.configuration.useDefaultsForSkieRuntime = true

        this.configuration[FlowInterop.Enabled] = originalFunctionDescriptor.configuration[FlowInterop.Enabled]
    }

    private fun FunctionDescriptor.hide() {
        context.doInPhase(SuspendGenerator.KotlinBridgingFunctionVisibilityConfigurationInitPhase) {
            val kirFunction = descriptorKirProvider.getFunction(this@hide)

            doInPhase(SuspendGenerator.KotlinBridgingFunctionVisibilityConfigurationFinalizePhase) {
                kirFunction.originalSirFunction.applyToEntireOverrideHierarchy {
                    visibility = SirVisibility.Internal
                }
            }
        }
    }

    private fun createBridgingFunction(
        functionDescriptor: FunctionDescriptor,
    ): FunctionDescriptor =
        context.declarationBuilder.createFunction(
            name = "Skie_Suspend__${nextBridgingFunctionIndex++}__${functionDescriptor.name.identifier}",
            namespace = context.declarationBuilder.getCustomNamespace("__SkieSuspendWrappers"),
            annotations = Annotations.EMPTY,
        ) {
            // WIP Replace with typeConstructor.parameters
            fun DeclarationDescriptor.typeParametersInScope(): List<TypeParameterDescriptor> {
                return when (this) {
                    is ClassifierDescriptorWithTypeParameters -> {
                        val declaredParameters = this.declaredTypeParameters

                        if (!isInner && containingDeclaration !is CallableDescriptor) {
                            declaredParameters
                        } else {
                            declaredParameters + containingDeclaration.typeParametersInScope()
                        }
                    }
                    is FunctionDescriptor -> this.typeParameters + this.containingDeclaration.typeParametersInScope()
                    else -> emptyList()
                }
            }

            val allTypeParameters = functionDescriptor.typeParametersInScope()
            val typeParameterMappingPairs = allTypeParameters.zip(allTypeParameters.copyIndexing(descriptor))

            val typeSubstitutor = TypeSubstitutor.create(
                TypeConstructorSubstitution.createByParametersMap(
                    typeParameterMappingPairs.associate { (from, into) ->
                        from to TypeProjectionImpl(into.defaultType)
                    },
                ),
            )

            valueParameters = functionDescriptor.createValueParametersForBridgingFunction(descriptor, typeSubstitutor)
            typeParameters = typeParameterMappingPairs.map { it.second }
            returnType = functionDescriptor.builtIns.unitType
            isSuspend = false
            modality = Modality.FINAL
            visibility = functionDescriptor.visibility
            body = {
                bodyGenerator.createBody(it, functionDescriptor)
            }
        }

    private fun FunctionDescriptor.createValueParametersForBridgingFunction(
        bridgingFunctionDescriptor: FunctionDescriptor,
        typeSubstitutor: TypeSubstitutor,
    ): List<ValueParameterDescriptor> = buildList {
        addDispatchReceiver(this@createValueParametersForBridgingFunction, bridgingFunctionDescriptor, typeSubstitutor)
        addExtensionReceiver(this@createValueParametersForBridgingFunction, bridgingFunctionDescriptor, typeSubstitutor)
        addCopiedValueParameters(this@createValueParametersForBridgingFunction, bridgingFunctionDescriptor, typeSubstitutor)
        addSuspendHandler(this@createValueParametersForBridgingFunction, bridgingFunctionDescriptor)
    }

    private fun MutableList<ValueParameterDescriptor>.addDispatchReceiver(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
        typeSubstitutor: TypeSubstitutor,
    ) {
        originalFunctionDescriptor.dispatchReceiverParameter?.let { dispatchReceiver ->
            val dispatchReceiverParameter = createValueParameter(
                owner = bridgingFunctionDescriptor,
                name = "dispatchReceiver".collisionFreeIdentifier(originalFunctionDescriptor.valueParameters),
                index = this.size,
                type = typeSubstitutor.safeSubstitute(dispatchReceiver.type, Variance.INVARIANT),
            )

            context.doInPhase(SuspendGenerator.FlowMappingConfigurationPhase) {
                configureFlowMappingForReceiver(dispatchReceiverParameter)
            }

            this.add(dispatchReceiverParameter)
        }
    }

    context(KirPhase.Context)
    private fun configureFlowMappingForReceiver(
        dispatchReceiverParameter: ValueParameterDescriptor,
    ) {
        val kirValueParameter = descriptorKirProvider.getValueParameter(dispatchReceiverParameter)

        kirValueParameter.configuration.flowMappingStrategy = kirValueParameter.configuration.flowMappingStrategy.limitFlowMappingToTypeArguments()
    }

    private fun MutableList<ValueParameterDescriptor>.addExtensionReceiver(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
        typeSubstitutor: TypeSubstitutor,
    ) {
        originalFunctionDescriptor.extensionReceiverParameter?.let { extensionReceiver ->
            val extensionReceiverParameter = createValueParameter(
                owner = bridgingFunctionDescriptor,
                name = "extensionReceiver".collisionFreeIdentifier(originalFunctionDescriptor.valueParameters),
                index = this.size,
                type = typeSubstitutor.safeSubstitute(extensionReceiver.type, Variance.INVARIANT),
            )

            configureFlowMappingForExtensionReceiver(originalFunctionDescriptor, extensionReceiverParameter)

            this.add(extensionReceiverParameter)
        }
    }

    private fun configureFlowMappingForExtensionReceiver(
        originalFunctionDescriptor: FunctionDescriptor,
        extensionReceiverParameter: ValueParameterDescriptor,
    ) {
        context.doInPhase(SuspendGenerator.FlowMappingConfigurationPhase) {
            val function = descriptorKirProvider.getFunction(originalFunctionDescriptor)

            val isExtensionReceiverUsedAsSwiftReceiver = function.scope == KirScope.Member &&
                originalFunctionDescriptor.dispatchReceiverParameter == null

            if (isExtensionReceiverUsedAsSwiftReceiver) {
                configureFlowMappingForReceiver(extensionReceiverParameter)
            }
        }
    }

    private fun MutableList<ValueParameterDescriptor>.addCopiedValueParameters(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
        typeSubstitutor: TypeSubstitutor,
    ) {
        originalFunctionDescriptor.valueParameters.forEach {
            val copy = it.copyWithoutDefaultValue(
                bridgingFunctionDescriptor,
                this.size,
                newType = typeSubstitutor.safeSubstitute(it.type, Variance.INVARIANT),
            )

            this.add(copy)
        }
    }

    private fun MutableList<ValueParameterDescriptor>.addSuspendHandler(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        val suspendHandlerParameter = originalFunctionDescriptor.createSuspendHandlerValueParameter(bridgingFunctionDescriptor, this.size)

        this.add(suspendHandlerParameter)
    }

    private fun FunctionDescriptor.createSuspendHandlerValueParameter(
        bridgingFunctionDescriptor: FunctionDescriptor,
        index: Int,
    ): ValueParameterDescriptor =
        createValueParameter(
            owner = bridgingFunctionDescriptor,
            name = "suspendHandler".collisionFreeIdentifier(this.valueParameters),
            index = index,
            type = suspendHandlerDescriptor.defaultType,
        )
}


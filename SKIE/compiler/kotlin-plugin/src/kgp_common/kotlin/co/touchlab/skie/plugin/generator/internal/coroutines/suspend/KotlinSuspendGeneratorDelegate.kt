package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.isMember
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.coroutines.suspend.kotlin.SuspendKotlinBridgeBodyGenerator
import co.touchlab.skie.plugin.generator.internal.util.ir.copy
import co.touchlab.skie.plugin.generator.internal.util.ir.copyIndexing
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.util.createValueParameter
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
import org.jetbrains.kotlin.types.FlexibleType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeConstructorSubstitution
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitution
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.inheritEnhancement
import org.jetbrains.kotlin.types.replace
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter

internal class KotlinSuspendGeneratorDelegate(
    private val module: SkieModule,
    private val declarationBuilder: DeclarationBuilder,
    descriptorProvider: DescriptorProvider,
) {

    private var nextBridgingFunctionIndex = 0

    private val suspendHandlerDescriptor: ClassDescriptor =
        descriptorProvider.exposedClasses
            .single { it.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.suspend.Skie_SuspendHandler") }

    private val bodyGenerator = SuspendKotlinBridgeBodyGenerator(suspendHandlerDescriptor)

    fun generateKotlinBridgingFunction(functionDescriptor: FunctionDescriptor): FunctionDescriptor {
        val bridgingFunctionDescriptor = createBridgingFunction(functionDescriptor)

        bridgingFunctionDescriptor.hide()

        return bridgingFunctionDescriptor
    }

    private fun FunctionDescriptor.hide() {
        module.configure {
            this.swiftModel.visibility = SwiftModelVisibility.Hidden
        }
    }

    private fun createBridgingFunction(
        functionDescriptor: FunctionDescriptor,
    ): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "Skie_Suspend__${nextBridgingFunctionIndex++}__${functionDescriptor.name.identifier}",
            namespace = declarationBuilder.getPackageNamespace(functionDescriptor),
            annotations = Annotations.EMPTY,
        ) {
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
                    }
                )
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

            module.configure {
                dispatchReceiverParameter.swiftModel.flowMappingStrategy = FlowMappingStrategy.TypeArgumentsOnly
            }

            this.add(dispatchReceiverParameter)
        }
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

            extensionReceiverParameter.configureExtensionReceiverFlowMapping(originalFunctionDescriptor)

            this.add(extensionReceiverParameter)
        }
    }

    private fun ValueParameterDescriptor.configureExtensionReceiverFlowMapping(originalFunctionDescriptor: FunctionDescriptor) {
        module.configure {
            val isExtensionReceiverUsedAsSwiftReceiver = originalFunctionDescriptor.swiftModel.scope.isMember &&
                    originalFunctionDescriptor.dispatchReceiverParameter == null

            if (isExtensionReceiverUsedAsSwiftReceiver) {
                this.swiftModel.flowMappingStrategy = FlowMappingStrategy.TypeArgumentsOnly
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

    private fun KotlinType.withTypeParametersReplaced(
        bridgingFunctionDescriptor: FunctionDescriptor,
        typeParameters: MutableList<TypeParameterDescriptor>,
    ): KotlinType {
        TypeUtils.getTypeParameterDescriptorOrNull(this)?.let {
            return if (it.containingDeclaration == bridgingFunctionDescriptor) {
                this
            } else {
                it.copy(
                    newOwner = bridgingFunctionDescriptor,
                    index = typeParameters.size,
                ).also { typeParameters.add(it) }.defaultType
            }
        }

        // TODO: Check if the type parameter is coming from outside, otherwise leave this type as it is
        return replaceArgumentsByParametersWith { originalTypeParameter, originalArgument ->
            if (originalArgument.type.isTypeParameter()) {
                originalTypeParameter.copy(
                    newOwner = bridgingFunctionDescriptor,
                    index = typeParameters.size,
                )
                    .also { typeParameters.add(it) }
                    .let { TypeProjectionImpl(it.defaultType) }
            } else {
                originalArgument
            }
        }
    }

    private inline fun KotlinType.replaceArgumentsByParametersWith(replacement: (TypeParameterDescriptor, TypeProjection) -> TypeProjection): KotlinType {
        val unwrapped = unwrap()
        return when (unwrapped) {
            is FlexibleType -> KotlinTypeFactory.flexibleType(
                unwrapped.lowerBound.replaceArgumentsByParametersWith(replacement),
                unwrapped.upperBound.replaceArgumentsByParametersWith(replacement)
            )
            is SimpleType -> unwrapped.replaceArgumentsByParametersWith(replacement)
        }.inheritEnhancement(unwrapped)
    }

    private inline fun SimpleType.replaceArgumentsByParametersWith(replacement: (TypeParameterDescriptor, TypeProjection) -> TypeProjection): SimpleType {
        if (constructor.parameters.isEmpty() || constructor.declarationDescriptor == null) return this

        val newArguments = constructor.parameters.zip(arguments, replacement)

        return replace(newArguments)
    }
}

class T : TypeSubstitution() {

    override fun get(key: KotlinType): TypeProjection? {
        TODO("Not yet implemented")
    }
}

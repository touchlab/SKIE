package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.isStatic
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.coroutines.suspend.kotlin.SuspendKotlinBridgeBodyGenerator
import co.touchlab.skie.plugin.generator.internal.util.ir.copy
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.util.createValueParameter
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

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
            valueParameters = functionDescriptor.createValueParametersForBridgingFunction(descriptor)
            typeParameters = functionDescriptor.typeParameters.copy(descriptor)
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
    ): List<ValueParameterDescriptor> {
        val parameters = mutableListOf<ValueParameterDescriptor>()

        parameters.addDispatchReceiver(this, bridgingFunctionDescriptor)
        parameters.addExtensionReceiver(this, bridgingFunctionDescriptor)
        parameters.addCopiedValueParameters(this, bridgingFunctionDescriptor)
        parameters.addSuspendHandler(this, bridgingFunctionDescriptor)

        return parameters
    }

    private fun MutableList<ValueParameterDescriptor>.addDispatchReceiver(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        originalFunctionDescriptor.dispatchReceiverParameter?.let { dispatchReceiver ->
            val dispatchReceiverParameter = createValueParameter(
                owner = bridgingFunctionDescriptor,
                name = "dispatchReceiver".collisionFreeIdentifier(originalFunctionDescriptor.valueParameters),
                index = this.size,
                type = dispatchReceiver.type
            )

            module.configure {
                dispatchReceiverParameter.swiftModel.isFlowMappingEnabled = false
            }

            this.add(dispatchReceiverParameter)
        }
    }

    private fun MutableList<ValueParameterDescriptor>.addExtensionReceiver(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        originalFunctionDescriptor.extensionReceiverParameter?.let { extensionReceiver ->
            val extensionReceiverParameter = createValueParameter(
                owner = bridgingFunctionDescriptor,
                name = "extensionReceiver".collisionFreeIdentifier(originalFunctionDescriptor.valueParameters),
                index = this.size,
                type = extensionReceiver.type
            )

            module.configure {
                extensionReceiverParameter.swiftModel.isFlowMappingEnabled =
                    originalFunctionDescriptor.swiftModel.scope.isStatic || originalFunctionDescriptor.dispatchReceiverParameter != null
            }

            this.add(extensionReceiverParameter)
        }
    }

    private fun MutableList<ValueParameterDescriptor>.addCopiedValueParameters(
        originalFunctionDescriptor: FunctionDescriptor,
        bridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        originalFunctionDescriptor.valueParameters.forEach {
            val copy = it.copyWithoutDefaultValue(bridgingFunctionDescriptor, this.size)

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


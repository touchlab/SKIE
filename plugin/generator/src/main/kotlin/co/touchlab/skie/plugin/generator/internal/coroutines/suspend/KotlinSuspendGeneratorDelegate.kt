package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.coroutines.suspend.kotlin.SuspendKotlinBridgeBodyGenerator
import co.touchlab.skie.plugin.generator.internal.util.ir.copy
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createFunction
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType

internal class KotlinSuspendGeneratorDelegate(
    private val module: SkieModule,
    private val declarationBuilder: DeclarationBuilder,
    descriptorProvider: DescriptorProvider,
) {

    private var nextBridgingFunctionIndex = 0

    private val suspendHandlerDescriptor: ClassDescriptor =
        descriptorProvider.transitivelyExposedClasses
            .single { it.fqNameSafe == FqName("co.touchlab.skie.runtime.coroutines.Skie_SuspendHandler") }

    private val bodyGenerator = SuspendKotlinBridgeBodyGenerator(suspendHandlerDescriptor)

    fun generateKotlinBridgingFunction(functionDescriptor: FunctionDescriptor): FunctionDescriptor {
        val bridgingFunctionDescriptor = createBridgingFunction(functionDescriptor)

        hideAndRenameBridgingFunction(bridgingFunctionDescriptor, functionDescriptor)

        return bridgingFunctionDescriptor
    }

    private fun hideAndRenameBridgingFunction(
        bridgingFunctionDescriptor: FunctionDescriptor,
        originalFunctionDescriptor: FunctionDescriptor,
    ) {
        module.configure {
            bridgingFunctionDescriptor.swiftModel.visibility = SwiftModelVisibility.Hidden
            bridgingFunctionDescriptor.swiftModel.identifier = originalFunctionDescriptor.suspendWrapperFunctionIdentifier
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

    private fun createValueParameter(
        owner: FunctionDescriptor,
        name: Name,
        index: Int,
        type: KotlinType,
    ): ValueParameterDescriptor =
        ValueParameterDescriptorImpl(
            containingDeclaration = owner,
            original = null,
            index = index,
            annotations = Annotations.EMPTY,
            name = name,
            outType = type,
            declaresDefaultValue = false,
            isCrossinline = false,
            isNoinline = false,
            varargElementType = null,
            source = SourceElement.NO_SOURCE,
        )
}

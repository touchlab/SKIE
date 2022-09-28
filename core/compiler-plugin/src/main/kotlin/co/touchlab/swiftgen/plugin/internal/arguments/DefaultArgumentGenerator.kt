package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.SwiftFileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.ir.IrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class DefaultArgumentGenerator(
    private val irBuilder: IrBuilder,
    swiftFileBuilderFactory: SwiftFileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
) : BaseGenerator(swiftFileBuilderFactory, namespaceProvider, configuration) {

    override fun generate(descriptorProvider: DescriptorProvider) {
        descriptorProvider.classDescriptors
            .filter { it.getConfiguration(ConfigurationKeys.ExperimentalFeatures.Enabled) }
            .forEach { classDescriptor ->
                classDescriptor.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
                    .filterIsInstance<SimpleFunctionDescriptor>()
                    .filter { it.visibility == DescriptorVisibilities.PUBLIC }
                    .filter { functionDescriptor ->
                        functionDescriptor.valueParameters.any { it.declaresDefaultValue() }
                    }
                    .filter { it.dispatchReceiverParameter != null }
                    .filter { it.extensionReceiverParameter == null }
                    .filter { it.typeParameters.isEmpty() }
                    .forEach {
                        generateOverload(it)
                    }
            }
    }

    private fun generateOverload(function: SimpleFunctionDescriptor) {
        val nonDefaultParameters = function.valueParameters.filterNot { it.hasDefaultValue() }

        irBuilder.createFunction(function.name) { overload ->
            extensionReceiverParameter = function.dispatchReceiverParameter
            valueParameters = nonDefaultParameters.mapIndexed { index, valueParameter ->
                valueParameter.copy(overload, valueParameter.name, index)
            }
            returnType = function.returnTypeOrNothing

            body = { overloadIr ->
                val functionIr = referenceSimpleFunction(function)

                irBlockBody {
                    +irReturn(
                        irCall(functionIr).apply {
                            dispatchReceiver = overloadIr.extensionReceiverParameter?.let { irGet(it) }
                            overloadIr.valueParameters.forEach {
                                putValueArgument(it.index, irGet(it))
                            }
                        }
                    )
                }
            }
        }
    }
}

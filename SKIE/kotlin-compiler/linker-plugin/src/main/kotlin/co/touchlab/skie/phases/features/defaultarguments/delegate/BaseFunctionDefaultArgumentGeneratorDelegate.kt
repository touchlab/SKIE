@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.irbuilder.createFunction
import co.touchlab.skie.kir.irbuilder.getNamespace
import co.touchlab.skie.kir.irbuilder.util.copyIndexing
import co.touchlab.skie.kir.irbuilder.util.copyWithoutDefaultValue
import co.touchlab.skie.phases.FrontendIrPhase
import co.touchlab.skie.phases.KotlinIrPhase
import co.touchlab.skie.phases.descriptorKirProvider
import co.touchlab.skie.phases.features.defaultarguments.DefaultArgumentGenerator
import co.touchlab.skie.phases.mapper
import co.touchlab.skie.phases.skieSymbolTable
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.util.SharedCounter
import co.touchlab.skie.util.isComposable
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.types.TypeConstructorSubstitution
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.Variance

abstract class BaseFunctionDefaultArgumentGeneratorDelegate(
    context: FrontendIrPhase.Context,
    private val sharedCounter: SharedCounter,
) : BaseDefaultArgumentGeneratorDelegate(context) {

    context(FrontendIrPhase.Context)
    override fun generate() {
        descriptorProvider.allSupportedFunctions()
            .filter { it.isInteropEnabled }
            .filterNot { it.isComposable }
            .filter { it.hasDefaultArguments }
            .filter { mapper.isBaseMethod(it) }
            .forEach {
                generateOverloads(it)
            }
    }

    protected abstract fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor>

    context(FrontendIrPhase.Context)
    private fun generateOverloads(function: SimpleFunctionDescriptor) {
        function.forEachDefaultArgumentOverload { overloadParameters ->
            generateOverload(function, overloadParameters)
        }
    }

    context(FrontendIrPhase.Context)
    private fun generateOverload(
        function: SimpleFunctionDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ) {
        val newFunction = generateOverloadWithUniqueName(function, parameters)

        registerOverload(newFunction, function)

        removeManglingOfOverload(newFunction, function)
    }

    context(FrontendIrPhase.Context)
    private fun generateOverloadWithUniqueName(
        function: SimpleFunctionDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ): FunctionDescriptor =
        declarationBuilder.createFunction(
            name = "${function.name.identifier}${uniqueNameSubstring}${sharedCounter.next()}",
            namespace = declarationBuilder.getNamespace(function),
            annotations = function.annotations,
        ) {
            val typeParameterMappingPairs = function.typeParameters.zip(function.typeParameters.copyIndexing(descriptor))
            val typeParameterSubstitutor = TypeSubstitutor.create(
                TypeConstructorSubstitution.createByParametersMap(
                    typeParameterMappingPairs.associate { (from, into) ->
                        from to TypeProjectionImpl(into.defaultType)
                    },
                ),
            )

            descriptor.configuration.overwriteBy(function.configuration)

            dispatchReceiverParameter = function.dispatchReceiverParameter
            extensionReceiverParameter = function.extensionReceiverParameter
                ?.copy(descriptor)
                ?.substitute(typeParameterSubstitutor)
            typeParameters = typeParameterMappingPairs.map { it.second }
            valueParameters = parameters.mapIndexed { index, parameter ->
                parameter.copyWithoutDefaultValue(
                    newOwner = descriptor,
                    newIndex = index,
                    newType = typeParameterSubstitutor.safeSubstitute(parameter.type, Variance.INVARIANT),
                )
            }
            returnType = function.returnTypeOrNothing
            isInline = function.isInline
            isSuspend = function.isSuspend
            modality = Modality.FINAL
            body = { overloadIr ->
                getOverloadBody(function, overloadIr)
            }
        }

    context(KotlinIrPhase.Context, DeclarationIrBuilder)
    private fun getOverloadBody(
        originalFunction: FunctionDescriptor, overloadIr: IrFunction,
    ): IrBody {
        val originalFunctionSymbol = skieSymbolTable.descriptorExtension.referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(originalFunctionSymbol).apply {
                    dispatchReceiver = overloadIr.dispatchReceiverParameter?.let { irGet(it) }
                    extensionReceiver = overloadIr.extensionReceiverParameter?.let { irGet(it) }
                    passArgumentsWithMatchingNames(overloadIr)
                },
            )
        }
    }

    private fun registerOverload(overloadDescriptor: FunctionDescriptor, function: SimpleFunctionDescriptor) {
        context.doInPhase(DefaultArgumentGenerator.RegisterOverloadsPhase) {
            val overloadKirFunction = descriptorKirProvider.getFunction(overloadDescriptor)

            descriptorKirProvider.getFunction(function).defaultArgumentsOverloads.add(overloadKirFunction)
        }
    }

    private fun removeManglingOfOverload(overloadDescriptor: FunctionDescriptor, function: SimpleFunctionDescriptor) {
        context.doInPhase(DefaultArgumentGenerator.RemoveManglingOfOverloadsInitPhase) {
            val overloadFunction = descriptorKirProvider.getFunction(overloadDescriptor)
            val baseFunction = descriptorKirProvider.getFunction(function)

            doInPhase(DefaultArgumentGenerator.RemoveManglingOfOverloadsFinalizePhase) {
                overloadFunction.originalSirFunction.identifier = baseFunction.originalSirFunction.identifier
                overloadFunction.primarySirFunction.identifier = baseFunction.primarySirFunction.identifier
            }
        }
    }
}

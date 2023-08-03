@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.DescriptorRegistrationScope
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.generator.internal.util.InternalDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.ir.copy
import co.touchlab.skie.plugin.generator.internal.util.ir.copyIndexing
import co.touchlab.skie.plugin.generator.internal.util.ir.copyWithoutDefaultValue
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.createFunction
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.getNamespace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsByParametersWith

internal abstract class BaseFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    private val descriptorProvider: InternalDescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    private val sharedCounter: SharedCounter,
) : BaseDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder) {

    override fun generate() {
        descriptorProvider.allSupportedFunctions()
            .filter { it.isInteropEnabled }
            .filter { it.hasDefaultArguments }
            .filter { descriptorProvider.mapper.isBaseMethod(it) }
            .forEach {
                generateOverloads(it)
            }
    }

    protected abstract fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor>

    private fun generateOverloads(function: SimpleFunctionDescriptor) {
        function.forEachDefaultArgumentOverload { overloadParameters ->
            generateOverload(function, overloadParameters)
        }
    }

    private fun generateOverload(
        function: SimpleFunctionDescriptor,
        parameters: List<ValueParameterDescriptor>,
    ) {
        val newFunction = generateOverloadWithUniqueName(function, parameters)

        renameOverloadedFunction(newFunction, function)
    }

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
                    }
                )
            )

            dispatchReceiverParameter = function.dispatchReceiverParameter
            extensionReceiverParameter = function.extensionReceiverParameter
                ?.copy(descriptor)
                ?.substitute(typeParameterSubstitutor)
            typeParameters = typeParameterMappingPairs.map { it.second }
            valueParameters = parameters.mapIndexed { index, parameter ->
                parameter.copyWithoutDefaultValue(
                    newOwner = descriptor,
                    newIndex = index,
                    newType = typeParameterSubstitutor.safeSubstitute(parameter.type, Variance.INVARIANT)
                )
            } // parameters.copyWithoutDefaultValue(descriptor, typeParameters)
            returnType = function.returnTypeOrNothing
            isInline = function.isInline
            isSuspend = function.isSuspend
            modality = Modality.FINAL
            body = { overloadIr ->
                getOverloadBody(function, overloadIr)
            }
        }

    context(IrPluginContext, DeclarationIrBuilder)
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun getOverloadBody(
        originalFunction: FunctionDescriptor, overloadIr: IrFunction,
    ): IrBody {
        val originalFunctionSymbol = symbolTable.referenceSimpleFunction(originalFunction)

        return irBlockBody {
            +irReturn(
                irCall(originalFunctionSymbol).apply {
                    dispatchReceiver = overloadIr.dispatchReceiverParameter?.let { irGet(it) }
                    extensionReceiver = overloadIr.extensionReceiverParameter?.let { irGet(it) }
                    passArgumentsWithMatchingNames(overloadIr)
                }
            )
        }
    }

    private fun renameOverloadedFunction(overloadDescriptor: FunctionDescriptor, function: SimpleFunctionDescriptor) {
        skieContext.module.configure {
            overloadDescriptor.swiftModel.identifier = function.swiftModel.identifier

            val numberOfDefaultArguments = function.valueParameters.size - overloadDescriptor.valueParameters.size

            overloadDescriptor.swiftModel.collisionResolutionStrategy = CollisionResolutionStrategy.Remove(numberOfDefaultArguments)
        }
    }

//    private fun ReceiverParameterDescriptor.copy(
//        newOwner: DeclarationDescriptor = this.containingDeclaration,
//        newType:
//    ): ReceiverParameterDescriptor {
//
//    }

//    private fun KotlinType.withTypeParametersReplaced(
//        typeParameterMapping: Map<TypeParameterDescriptor, TypeParameterDescriptor>,
//    ): KotlinType {
//
//        replaceArgumentsByParametersWith {
//
//        }
//    }
}

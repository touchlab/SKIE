package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.type.stableSpec
import co.touchlab.skie.plugin.api.model.type.translation.SwiftGenericTypeUsageModel
import co.touchlab.skie.plugin.api.model.type.translation.SwiftKotlinTypeClassTypeModel
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

internal class SwiftSuspendGeneratorDelegate(
    private val module: SkieModule,
) : SwiftPoetExtensionContainer {

    fun generateSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        module.generateCode(originalFunctionDescriptor) {
            if (originalFunctionDescriptor.swiftModel.visibility.isRemoved || kotlinBridgingFunctionDescriptor.swiftModel.visibility.isRemoved) {
                return@generateCode
            }

            val bridgeModel = BridgeModel(
                originalFunction = originalFunctionDescriptor.swiftModel,
                asyncOriginalFunction = originalFunctionDescriptor.asyncSwiftModel,
                kotlinBridgingFunction = kotlinBridgingFunctionDescriptor.swiftModel,
            )

            addExtension(
                ExtensionSpec.builder(DeclaredTypeName.qualifiedLocalTypeName(bridgeModel.extensionScopeNameForBridgingFunction))
                    .addModifiers(Modifier.PUBLIC)
                    .addSwiftBridgingFunction(bridgeModel)
                    .build()
            )
        }
    }

    private val BridgeModel.extensionScopeNameForBridgingFunction: String
        get() =
            if (this.isFromGenericClass) {
                this.kotlinBridgingFunction.receiver.bridgedOrStableFqName
            } else {
                this.originalFunction.receiver.bridgedOrStableFqName
            }

    private val BridgeModel.isFromGenericClass: Boolean
        get() = (this.originalFunction.receiver as? SwiftKotlinTypeClassTypeModel)?.typeArguments?.isNotEmpty() ?: false

    private val BridgeModel.typeParameterNames: List<String>
        get() {
            val receiver = this.originalFunction.receiver as? SwiftKotlinTypeClassTypeModel ?: return emptyList()

            return receiver.typeArguments.filterIsInstance<SwiftGenericTypeUsageModel>().map { it.stableFqName }
        }

    private fun ExtensionSpec.Builder.addSwiftBridgingFunction(bridgeModel: BridgeModel): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                FunctionSpec.builder(bridgeModel.originalFunction.identifier)
                    .setScope(bridgeModel)
                    .addAttribute(AttributeSpec.available("iOS" to "13", "macOS" to "10.15", "watchOS" to "6", "tvOS" to "13", "*" to ""))
                    .async(true)
                    .throws(true)
                    .addTypeParameters(bridgeModel)
                    .addValueParameters(bridgeModel)
                    .addReturnType(bridgeModel)
                    .addFunctionBody(bridgeModel)
                    .build()
            )
        }

    private fun FunctionSpec.Builder.setScope(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            if (bridgeModel.originalFunction.scope == KotlinCallableMemberSwiftModel.Scope.Static || bridgeModel.isFromGenericClass) {
                this.addModifiers(Modifier.STATIC)
            }
        }

    private fun FunctionSpec.Builder.addTypeParameters(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            val typeVariables = bridgeModel.typeParameterNames
                .map { TypeVariableName.typeVariable(it, TypeVariableName.Bound(ANY_OBJECT)) }

            addTypeVariables(typeVariables)
        }

    private fun FunctionSpec.Builder.addValueParameters(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            if (bridgeModel.isFromGenericClass) {
                addDispatchReceiverParameterForGenericClass(bridgeModel)
            }

            bridgeModel.bridgedParameters.forEach {
                addValueParameter(it)
            }
        }

    private val BridgeModel.bridgedParameters: List<KotlinValueParameterSwiftModel>
        get() = this.originalFunction.valueParameters.filter { it.origin != KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

    private fun FunctionSpec.Builder.addDispatchReceiverParameterForGenericClass(bridgeModel: BridgeModel) {
        val receiverSwiftModel = bridgeModel.originalFunction.receiver

        addParameter(
            ParameterSpec.builder("_", bridgeModel.genericClassDispatchReceiverParameterName, receiverSwiftModel.stableSpec)
                .build()
        )
    }

    private val BridgeModel.genericClassDispatchReceiverParameterName: String
        get() = "dispatchReceiver".collisionFreeIdentifier(originalFunction.valueParameters.map { it.argumentLabel })

    private fun FunctionSpec.Builder.addValueParameter(parameter: KotlinValueParameterSwiftModel) {
        addParameter(
            ParameterSpec.builder(parameter.argumentLabel, parameter.parameterName, parameter.type.stableSpec)
                .build()
        )
    }

    private fun FunctionSpec.Builder.addReturnType(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            returns(bridgeModel.asyncOriginalFunction.returnType.stableSpec)
        }

    private fun FunctionSpec.Builder.addFunctionBody(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            addCode(
                CodeBlock.builder()
                    .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                    .indent()
                    .apply {
                        addStatement(
                            "%N.%N(${bridgeModel.valueParametersPlaceholders})",
                            bridgeModel.kotlinBridgingFunction.receiver.stableFqName,
                            bridgeModel.kotlinBridgingFunction.reference,
                            *bridgeModel.argumentsForBridgingCall.toTypedArray(),
                        )
                    }
                    .unindent()
                    .addStatement("}")
                    .build()
            )
        }

    private val BridgeModel.valueParametersPlaceholders: String
        get() = (this.argumentsForBridgingCall.map { "%N" } + "$0").joinToString(", ")

    private val BridgeModel.argumentsForBridgingCall: List<String>
        get() {
            val arguments = mutableListOf<String>()

            arguments.addDispatchReceiver(this)

            this.bridgedParameters.forEachIndexed { index, parameter ->
                if (isFromGenericClass) {
                    val erasedParameterType = kotlinBridgingFunction.valueParameters[index + 1].type.stableFqName

                    arguments.add(
                        listOfNotNull(
                            parameter.parameterName,
                            " as! $erasedParameterType".takeIf { parameter.type.stableFqName != erasedParameterType },
                        ).joinToString("")
                    )
                } else {
                    arguments.add(parameter.parameterName)
                }
            }

            return arguments
        }

    private fun MutableList<String>.addDispatchReceiver(bridgeModel: BridgeModel) {
        if (bridgeModel.originalFunction.scope != KotlinCallableMemberSwiftModel.Scope.Member) {
            return
        }

        if (bridgeModel.isFromGenericClass) {
            val dispatchReceiverErasedType = bridgeModel.kotlinBridgingFunction.valueParameters.first().type.stableFqName

            add(bridgeModel.genericClassDispatchReceiverParameterName + " as! " + dispatchReceiverErasedType)
        } else {
            add("self")
        }
    }

    private data class BridgeModel(
        val originalFunction: KotlinFunctionSwiftModel,
        val asyncOriginalFunction: KotlinFunctionSwiftModel,
        val kotlinBridgingFunction: KotlinFunctionSwiftModel,
    )
}

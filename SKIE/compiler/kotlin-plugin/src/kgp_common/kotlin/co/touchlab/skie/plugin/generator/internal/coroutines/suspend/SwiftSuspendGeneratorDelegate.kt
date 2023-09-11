package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.element.SirExtension
import co.touchlab.skie.plugin.api.sir.element.SirTypeDeclaration
import co.touchlab.skie.plugin.generator.internal.util.swift.addFunctionBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

// WIP Test suspend in enum
internal class SwiftSuspendGeneratorDelegate(
    private val module: SkieModule,
) {

    private val skieClassSuspendGenerator = SkieClassSuspendGenerator()

    fun generateSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        module.configure(SkieModule.Ordering.Last) {
            if (originalFunctionDescriptor.swiftModel.visibility.isRemoved || kotlinBridgingFunctionDescriptor.swiftModel.visibility.isRemoved) {
                return@configure
            }

            val bridgeModel = BridgeModel(
                originalFunction = originalFunctionDescriptor.swiftModel,
                asyncOriginalFunction = originalFunctionDescriptor.asyncSwiftModel,
                kotlinBridgingFunction = kotlinBridgingFunctionDescriptor.swiftModel,
            )

            val extension = SirExtension(
                typeDeclaration = bridgeModel.extensionTypeDeclarationForBridgingFunction,
                parent = sirProvider.getFile(bridgeModel.originalFunction.owner!!)
            )

            extension.addSwiftBridgingFunction(bridgeModel)
        }
    }

    context(SwiftModelScope)
    private val BridgeModel.extensionTypeDeclarationForBridgingFunction: SirTypeDeclaration
        get() {
            val owner = this.originalFunction.owner ?: error("No owner for bridging function")

            return if (this.isFromGenericClass) {
                skieClassSuspendGenerator.getOrCreateSkieClass(owner)
            } else {
                owner.primarySirClass
            }
        }

    private val BridgeModel.isFromGenericClass: Boolean
        get() = this.originalFunction.owner?.primarySirClass?.typeParameters?.isEmpty()?.not() ?: false

    private fun SirExtension.addSwiftBridgingFunction(bridgeModel: BridgeModel) {
        this.swiftPoetBuilderModifications.add {
            addFunction(
                FunctionSpec.builder(bridgeModel.originalFunction.identifier)
                    .setScope(bridgeModel)
                    .addAttribute(AttributeSpec.available("iOS" to "13", "macOS" to "10.15", "watchOS" to "6", "tvOS" to "13", "*" to ""))
                    .async(true)
                    .throws(true)
                    .addValueParameters(bridgeModel)
                    .addReturnType(bridgeModel)
                    .addFunctionBody(bridgeModel)
                    .build(),
            )
        }
    }

    private fun FunctionSpec.Builder.setScope(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            if (bridgeModel.originalFunction.scope == KotlinCallableMemberSwiftModel.Scope.Static) {
                this.addModifiers(Modifier.STATIC)
            }
        }

    private fun FunctionSpec.Builder.addValueParameters(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            bridgeModel.bridgedParameters.forEach {
                addValueParameter(it)
            }
        }

    private val BridgeModel.bridgedParameters: List<KotlinValueParameterSwiftModel>
        get() = this.originalFunction.valueParameters.filter { it.origin != KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

    private fun FunctionSpec.Builder.addValueParameter(parameter: KotlinValueParameterSwiftModel) {
        addParameter(
            ParameterSpec.builder(parameter.argumentLabel, parameter.parameterName, parameter.type.toSwiftPoetUsage())
                .build(),
        )
    }

    private fun FunctionSpec.Builder.addReturnType(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            returns(bridgeModel.asyncOriginalFunction.returnType.toSwiftPoetUsage())
        }

    private fun FunctionSpec.Builder.addFunctionBody(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.addFunctionBodyWithErrorTypeHandling(bridgeModel.kotlinBridgingFunction) {
            addCode(
                CodeBlock.builder()
                    .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                    .indent()
                    .apply {
                        addStatement(
                            "%T.%N(%L)",
                            bridgeModel.kotlinBridgingFunction.receiver.toSwiftPoetUsage(),
                            bridgeModel.kotlinBridgingFunction.reference,
                            bridgeModel.argumentsForBridgingCall,
                        )
                    }
                    .unindent()
                    .addStatement("}")
                    .build(),
            )
        }

    private val BridgeModel.argumentsForBridgingCall: CodeBlock
        get() {
            val arguments = mutableListOf<CodeBlock>()

            arguments.addDispatchReceiver(this)

            this.bridgedParameters.forEachIndexed { index, parameter ->
                if (isFromGenericClass) {
                    // WIP
                    val erasedParameterType = kotlinBridgingFunction.valueParameters[index + 1].type.toSwiftPoetUsage()
                        // Ideally we wouldn't need this, but in case the parameter is a lambda, it will have the escaping attribute which we can't use elsewhere.
                        .removingEscapingAttribute()

                    if (parameter.type.toSwiftPoetUsage() != erasedParameterType) {
                        arguments.add(CodeBlock.of("%N as! %T", parameter.parameterName, erasedParameterType))
                    } else {
                        arguments.add(CodeBlock.of("%N", parameter.parameterName))
                    }
                } else {
                    arguments.add(CodeBlock.of("%N", parameter.parameterName))
                }
            }

            arguments.addSuspendHandlerParameter()

            return arguments.joinToCode()
        }

    private fun MutableList<CodeBlock>.addDispatchReceiver(bridgeModel: BridgeModel) {
        if (bridgeModel.originalFunction.scope != KotlinCallableMemberSwiftModel.Scope.Member) {
            return
        }

        if (bridgeModel.isFromGenericClass) {
            val dispatchReceiverErasedType = bridgeModel.kotlinBridgingFunction.valueParameters.first().type.toSwiftPoetUsage()

            add(CodeBlock.of("%N as! %T", SkieClassSuspendGenerator.kotlinObjectVariableName, dispatchReceiverErasedType))
        } else {
            add(CodeBlock.of("self"))
        }
    }

    private fun MutableList<CodeBlock>.addSuspendHandlerParameter() = add(CodeBlock.of("$0"))

    private data class BridgeModel(
        val originalFunction: KotlinFunctionSwiftModel,
        val asyncOriginalFunction: KotlinFunctionSwiftModel,
        val kotlinBridgingFunction: KotlinFunctionSwiftModel,
    )

    // WIP
    private fun TypeName.removingEscapingAttribute(): TypeName {
        return when (this) {
            is FunctionTypeName -> this.copy(attributes = this.attributes - AttributeSpec.ESCAPING)
            else -> this
        }
    }
}

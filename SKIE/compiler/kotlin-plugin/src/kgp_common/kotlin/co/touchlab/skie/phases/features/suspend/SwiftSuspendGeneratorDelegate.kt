package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.isRemoved
import co.touchlab.skie.util.swift.addFunctionBodyWithErrorTypeHandling
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class SwiftSuspendGeneratorDelegate(
    private val context: DescriptorModificationPhase.Context,
) {

    private val skieClassSuspendGenerator = SkieClassSuspendGenerator()

    fun generateSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        context.doInPhase(SuspendGenerator.SwiftBridgeGeneratorPhase) {
            if (originalFunctionDescriptor.swiftModel.visibility.isRemoved || kotlinBridgingFunctionDescriptor.swiftModel.visibility.isRemoved) {
                return@doInPhase
            }

            val bridgeModel = BridgeModel(
                originalFunction = originalFunctionDescriptor.swiftModel,
                asyncOriginalFunction = originalFunctionDescriptor.asyncSwiftModel,
                kotlinBridgingFunction = kotlinBridgingFunctionDescriptor.swiftModel,
            )

            SirExtension(
                classDeclaration = bridgeModel.extensionTypeDeclarationForBridgingFunction,
                parent = sirProvider.getFile(bridgeModel.originalFunction.owner!!),
            ).apply {
                addSwiftBridgingFunction(bridgeModel)
            }
        }
    }

    context(SwiftModelScope)
    private val BridgeModel.extensionTypeDeclarationForBridgingFunction: SirClass
        get() {
            val owner = this.originalFunction.owner ?: error("No owner for bridging function")

            return if (this.isFromGenericClass) {
                skieClassSuspendGenerator.getOrCreateSkieClass(owner)
            } else {
                owner.kotlinSirClass
            }
        }
}

fun SirFunction.addAvailabilityForAsync() {
    attributes.add("available(iOS 13, macOS 10.15, watchOS 6, tvOS 13, *)")
}

private fun SirExtension.addSwiftBridgingFunction(bridgeModel: BridgeModel) {
    SirFunction(
        identifier = bridgeModel.originalFunction.identifier,
        returnType = bridgeModel.asyncOriginalFunction.returnType,
        scope = if (bridgeModel.originalFunction.scope == KotlinCallableMemberSwiftModel.Scope.Static) SirScope.Static else SirScope.Member,
    ).apply {
        // WIP Replace with copyValueParametersFrom once Functions have Sir
        bridgeModel.bridgedParameters.forEach {
            SirValueParameter(
                label = it.argumentLabel,
                name = it.parameterName,
                type = it.type,
            )
        }

        addAvailabilityForAsync()
        isAsync = true
        throws = true

        addSwiftBridgingFunctionBody(bridgeModel)
    }
}

private fun SirFunction.addSwiftBridgingFunctionBody(bridgeModel: BridgeModel) {
    addFunctionBodyWithErrorTypeHandling(bridgeModel.kotlinBridgingFunction) {
        addCode(
            CodeBlock.builder()
                .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                .indent()
                .apply {
                    addStatement(
                        "%T.%N(%L)",
                        bridgeModel.kotlinBridgingFunction.receiver.toSwiftPoetTypeName(),
                        bridgeModel.kotlinBridgingFunction.reference,
                        bridgeModel.argumentsForBridgingCall,
                    )
                }
                .unindent()
                .addStatement("}")
                .build(),
        )
    }
}

private val BridgeModel.argumentsForBridgingCall: CodeBlock
    get() {
        val arguments = mutableListOf<CodeBlock>()

        arguments.addDispatchReceiver(this)

        this.bridgedParameters.forEachIndexed { index, parameter ->
            if (isFromGenericClass) {
                val erasedParameterType = kotlinBridgingFunction.valueParameters[index + 1].type.toSwiftPoetTypeName()
                    // Ideally we wouldn't need this, but in case the parameter is a lambda, it will have the escaping attribute which we can't use elsewhere.
                    .removingEscapingAttribute()

                if (parameter.type.toSwiftPoetTypeName() != erasedParameterType) {
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

    val dispatchReceiverErasedType by lazy {
        bridgeModel.kotlinBridgingFunction.valueParameters.first().type.toSwiftPoetTypeName()
    }

    if (bridgeModel.isFromGenericClass) {
        add(CodeBlock.of("%N as! %T", SkieClassSuspendGenerator.kotlinObjectVariableName, dispatchReceiverErasedType))
    } else if (bridgeModel.isFromBridgedClass) {
        add(CodeBlock.of("self as %T", dispatchReceiverErasedType))
    } else {
        add(CodeBlock.of("self"))
    }
}

private fun MutableList<CodeBlock>.addSuspendHandlerParameter() = add(CodeBlock.of("$0"))

private fun TypeName.removingEscapingAttribute(): TypeName {
    return when (this) {
        is FunctionTypeName -> this.copy(attributes = this.attributes - AttributeSpec.ESCAPING)
        else -> this
    }
}

private data class BridgeModel(
    val originalFunction: KotlinFunctionSwiftModel,
    val asyncOriginalFunction: KotlinFunctionSwiftModel,
    val kotlinBridgingFunction: KotlinFunctionSwiftModel,
)

private val BridgeModel.bridgedParameters: List<KotlinValueParameterSwiftModel>
    get() = this.originalFunction.valueParameters.filter { it.origin != KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

private val BridgeModel.isFromGenericClass: Boolean
    get() = this.originalFunction.owner?.kotlinSirClass?.typeParameters?.isEmpty()?.not() ?: false

private val BridgeModel.isFromBridgedClass: Boolean
    get() = this.originalFunction.owner?.bridgedSirClass != null

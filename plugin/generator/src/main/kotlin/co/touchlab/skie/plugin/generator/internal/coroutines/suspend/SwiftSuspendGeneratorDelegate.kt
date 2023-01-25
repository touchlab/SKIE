package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.function.reference
import co.touchlab.skie.plugin.api.model.type.stableSpec
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.util.CallableMemberSwiftType
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import co.touchlab.skie.plugin.generator.internal.util.swiftKind
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor

internal class SwiftSuspendGeneratorDelegate(
    private val module: SkieModule,
) : SwiftPoetExtensionContainer {

    fun generateSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        if (originalFunctionDescriptor.isFromGenericClass) {
            return
        }

        module.generateCode(originalFunctionDescriptor) {
            addExtension(
                ExtensionSpec.builder(DeclaredTypeName.qualifiedLocalTypeName(originalFunctionDescriptor.swiftModel.receiver.bridgedOrStableFqName))
                    .addModifiers(Modifier.PUBLIC)
                    .addSwiftBridgingFunction(originalFunctionDescriptor, kotlinBridgingFunctionDescriptor)
                    .build()
            )
        }
    }

    private val FunctionDescriptor.isFromGenericClass: Boolean
        get() {
            val classifier = this.dispatchReceiverParameter?.type?.constructor?.declarationDescriptor

            if (classifier !is ClassDescriptor) return false

            return classifier.kind == ClassKind.CLASS && classifier.declaredTypeParameters.isNotEmpty()
        }

    context(SwiftModelScope)
    private fun ExtensionSpec.Builder.addSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                FunctionSpec.builder(originalFunctionDescriptor.suspendWrapperFunctionIdentifier)
                    .setScope(originalFunctionDescriptor)
                    .addAttribute(AttributeSpec.available("iOS" to "13", "macOS" to "10.15", "watchOS" to "6", "tvOS" to "13", "*" to ""))
                    .async(true)
                    .throws(true)
                    .addValueParameters(originalFunctionDescriptor)
                    .addReturnType(originalFunctionDescriptor)
                    .addFunctionBody(originalFunctionDescriptor, kotlinBridgingFunctionDescriptor)
                    .build()
            )
        }

    private fun FunctionSpec.Builder.setScope(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            if (originalFunctionDescriptor.swiftKind == CallableMemberSwiftType.Function ||
                originalFunctionDescriptor.swiftKind == CallableMemberSwiftType.Extension.Interface
            ) {
                this.addModifiers(Modifier.STATIC)
            }
        }

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addValueParameters(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            addReceiversParameters(originalFunctionDescriptor)

            originalFunctionDescriptor.valueParameters.forEach {
                addValueParameter(it)
            }
        }

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addReceiversParameters(originalFunctionDescriptor: FunctionDescriptor) {
        when (originalFunctionDescriptor.swiftKind) {
            CallableMemberSwiftType.Extension.Interface -> {
                addReceiver(originalFunctionDescriptor.swiftReceiverParameterName, originalFunctionDescriptor.extensionReceiverParameter!!)
            }
            is CallableMemberSwiftType.Method -> {
                originalFunctionDescriptor.extensionReceiverParameter?.let {
                    addReceiver(originalFunctionDescriptor.swiftReceiverParameterName, it)
                }
            }
            else -> {}
        }
    }

    private val FunctionDescriptor.swiftReceiverParameterName: String
        get() = "receiver".collisionFreeIdentifier(this.valueParameters).identifier

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addReceiver(parameterName: String, receiverParameter: ReceiverParameterDescriptor) {
        val receiverSwiftModel = receiverParameter.swiftModel

        addParameter(
            ParameterSpec.builder("_", parameterName, receiverSwiftModel.stableSpec)
                .build()
        )
    }

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addValueParameter(valueParameter: ValueParameterDescriptor) {
        val parameterSwiftModel = valueParameter.swiftModel
        val parameterType = parameterSwiftModel.type

        addParameter(
            ParameterSpec.builder(parameterSwiftModel.argumentLabel, parameterSwiftModel.parameterName, parameterType.stableSpec)
                .build()
        )
    }

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addReturnType(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            val returnType = originalFunctionDescriptor.swiftModel.returnType
            returns(returnType.stableSpec)
        }

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addFunctionBody(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunction: FunctionDescriptor,
    ): FunctionSpec.Builder =
        this.apply {
            addCode(
                CodeBlock.builder()
                    .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                    .indent()
                    .apply {
                        addStatement(
                            "%N.%N(${originalFunctionDescriptor.valueParametersPlaceholders})",
                            kotlinBridgingFunction.swiftModel.receiver.stableFqName,
                            kotlinBridgingFunction.swiftModel.reference,
                            *originalFunctionDescriptor.argumentsForBridgingCall.toTypedArray(),
                        )
                    }
                    .unindent()
                    .addStatement("}")
                    .build()
            )
        }

    context(SwiftModelScope)
    private val FunctionDescriptor.valueParametersPlaceholders: String
        get() = (this.argumentsForBridgingCall.map { "%N" } + "$0").joinToString(", ")

    context(SwiftModelScope)
    private val FunctionDescriptor.argumentsForBridgingCall: List<String>
        get() {
            val arguments = mutableListOf<String>()

            arguments.addReceiversArguments(this)

            this.valueParameters.forEach {
                arguments.add(it.swiftModel.parameterName)
            }

            return arguments
        }

    context(SwiftModelScope)
    private fun MutableList<String>.addReceiversArguments(originalFunctionDescriptor: FunctionDescriptor) {
        when (originalFunctionDescriptor.swiftKind) {
            CallableMemberSwiftType.Extension.Class, CallableMemberSwiftType.Extension.Enum -> {
                add("self")
            }
            CallableMemberSwiftType.Extension.Interface -> {
                add(originalFunctionDescriptor.swiftReceiverParameterName)
            }
            is CallableMemberSwiftType.Method -> {
                add("self")
                originalFunctionDescriptor.extensionReceiverParameter?.let {
                    add(originalFunctionDescriptor.swiftReceiverParameterName)
                }
            }
            CallableMemberSwiftType.Function -> {}
        }
    }
}

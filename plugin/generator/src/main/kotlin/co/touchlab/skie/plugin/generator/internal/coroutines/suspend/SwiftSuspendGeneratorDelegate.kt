package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.model.function.name
import co.touchlab.skie.plugin.api.model.function.reference
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSpecUsage
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.api.util.qualifiedLocalTypeName
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.isTopLevelInPackage
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.types.typeUtil.isUnit

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
                ExtensionSpec.builder(DeclaredTypeName.qualifiedLocalTypeName(originalFunctionDescriptor.swiftModel.receiver.stableFqName))
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

    context(SwiftPoetScope)
    private fun ExtensionSpec.Builder.addSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                FunctionSpec.builder(originalFunctionDescriptor.swiftWrapperFunctionIdentifier)
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

    context(SwiftPoetScope)
    private val FunctionDescriptor.swiftWrapperFunctionIdentifier: String
        get() {
            val suffixForPreventingCollisions = this.swiftModel.name.removeSuffix(":)").takeLastWhile { it == '_' }

            return this.swiftModel.identifier + suffixForPreventingCollisions
        }

    private fun FunctionSpec.Builder.setScope(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            if (originalFunctionDescriptor.isTopLevelInPackage()) {
                this.addModifiers(Modifier.STATIC)
            }
        }

    context(SwiftPoetScope)
    private fun FunctionSpec.Builder.addValueParameters(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            originalFunctionDescriptor.valueParameters.forEach {
                addValueParameter(it)
            }
        }

    context(SwiftPoetScope)
    private fun FunctionSpec.Builder.addValueParameter(valueParameter: ValueParameterDescriptor): FunctionSpec.Builder =
        this.apply {
            val parameterTypeSpec = valueParameter.type.spec(KotlinTypeSpecUsage.ParameterType)

            val parameterBuilder = ParameterSpec.builder(valueParameter.name.asString(), parameterTypeSpec)

            if (parameterTypeSpec is FunctionTypeName) {
                parameterBuilder.addAttribute(AttributeSpec.ESCAPING)
            }

            addParameter(parameterBuilder.build())
        }

    context(SwiftPoetScope)
    private fun FunctionSpec.Builder.addReturnType(originalFunctionDescriptor: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            val returnType = originalFunctionDescriptor.returnTypeOrNothing.spec(KotlinTypeSpecUsage.ReturnType.SuspendFunction)
            returns(returnType)
        }

    context(SwiftPoetScope)
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

    private val FunctionDescriptor.valueParametersPlaceholders: String
        get() = (this.argumentsForBridgingCall.map { "%N" } + "$0").joinToString(", ")

    private val FunctionDescriptor.argumentsForBridgingCall: List<String>
        get() {
            val arguments = mutableListOf<String>()

            this.dispatchReceiverParameter?.let {
                arguments.add("self")
            }

            this.valueParameters.forEach {
                arguments.add(it.name.asString())
            }

            return arguments
        }
}

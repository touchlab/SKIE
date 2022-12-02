package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.api.SwiftPoetScope
import co.touchlab.skie.plugin.api.type.KotlinTypeSpecKind
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
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.calls.inference.returnTypeOrNothing
import org.jetbrains.kotlin.types.typeUtil.isUnit

internal class SwiftSuspendGeneratorDelegate(
    private val module: SkieModule,
) : SwiftPoetExtensionContainer {

    fun generateSwiftBridgingFunction(function: FunctionDescriptor, kotlinBridgingFunction: FunctionDescriptor) {
        module.generateCode(function) {
            addExtension(
                ExtensionSpec.builder(DeclaredTypeName.qualifiedLocalTypeName(function.swiftName.receiverName.qualifiedName))
                    .addModifiers(Modifier.PUBLIC)
                    .addSwiftBridgingFunction(function, kotlinBridgingFunction)
                    .build()
            )
        }
    }

    context(SwiftPoetScope)
        private fun ExtensionSpec.Builder.addSwiftBridgingFunction(
        function: FunctionDescriptor,
        kotlinBridgingFunction: FunctionDescriptor,
    ): ExtensionSpec.Builder =
        this.apply {
            addFunction(
                FunctionSpec.builder(function.swiftName.name)
                    .addModifiers(Modifier.STATIC)
                    .async(true)
                    .throws(true)
                    .addValueParameters(function)
                    .addReturnType(function)
                    .addFunctionBody(function, kotlinBridgingFunction)
                    .build()
            )
        }

    context(SwiftPoetScope)
        private fun FunctionSpec.Builder.addValueParameters(function: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            function.valueParameters.forEach {
                addValueParameter(it)
            }
        }

    context(SwiftPoetScope)
        private fun FunctionSpec.Builder.addValueParameter(valueParameter: ValueParameterDescriptor): FunctionSpec.Builder =
        this.apply {
            val parameterTypeSpec = valueParameter.type.spec(KotlinTypeSpecKind.BRIDGED)

            val parameterBuilder = ParameterSpec.builder(valueParameter.name.asString(), parameterTypeSpec)

            if (parameterTypeSpec is FunctionTypeName) {
                parameterBuilder.addAttribute(AttributeSpec.ESCAPING)
            }

            addParameter(parameterBuilder.build())
        }

    context(SwiftPoetScope)
        private fun FunctionSpec.Builder.addReturnType(function: FunctionDescriptor): FunctionSpec.Builder =
        this.apply {
            if (!function.returnTypeOrNothing.isUnit()) {
                val returnType = function.returnTypeOrNothing.spec(KotlinTypeSpecKind.SWIFT_GENERICS)

                returns(returnType)
            }
        }

    context(SwiftPoetScope)
        private fun FunctionSpec.Builder.addFunctionBody(
        function: FunctionDescriptor,
        kotlinBridgingFunction: FunctionDescriptor,
    ): FunctionSpec.Builder =
        this.apply {
            addCode(
                CodeBlock.builder()
                    .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                    .indent()
                    .apply {
                        val parametersPlaceholder = (function.valueParameters.map { "%N" } + "$0").joinToString(", ")

                        addStatement(
                            "%N.%N($parametersPlaceholder)",
                            kotlinBridgingFunction.swiftName.receiverName.qualifiedName,
                            kotlinBridgingFunction.swiftName.reference,
                            *function.valueParameters.map { it.name.asString() }.toTypedArray(),
                        )
                    }
                    .unindent()
                    .addStatement("}")
                    .build()
            )
        }
}

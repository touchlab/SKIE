package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrExtensibleDeclaration
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.sir.type.SwiftClassSirType
import co.touchlab.skie.plugin.api.sir.type.SwiftGenericTypeUsageSirType
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.joinToCode
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
                ExtensionSpec.builder(bridgeModel.extensionScopeForBridgingFunction.internalName.toSwiftPoetName())
                    .addModifiers(Modifier.PUBLIC)
                    .addSwiftBridgingFunction(bridgeModel)
                    .build()
            )
        }
    }

    private val BridgeModel.extensionScopeForBridgingFunction: SwiftIrExtensibleDeclaration
        get() =
            if (this.isFromGenericClass) {
                this.kotlinBridgingFunction.owner
            } else {
                this.originalFunction.owner
            }

    private val BridgeModel.isFromGenericClass: Boolean
        get() = (this.originalFunction.owner as? SwiftIrTypeDeclaration)?.typeParameters.isNullOrEmpty().not()

    private val BridgeModel.typeParameterNames: List<TypeVariableName>
        get() {
            val receiver = this.originalFunction.receiver as? SwiftClassSirType ?: return emptyList()

            return receiver.typeArguments.mapNotNull {
                (it as? SwiftGenericTypeUsageSirType)?.declaration?.toInternalSwiftPoetVariable()
            }
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
            addTypeVariables(bridgeModel.typeParameterNames)
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
        val receiver = bridgeModel.originalFunction.receiver

        addParameter(
            ParameterSpec.builder("_", bridgeModel.genericClassDispatchReceiverParameterName, receiver.toSwiftPoetUsage())
                .build()
        )
    }

    private val BridgeModel.genericClassDispatchReceiverParameterName: String
        get() = "dispatchReceiver".collisionFreeIdentifier(originalFunction.valueParameters.map { it.argumentLabel })

    private fun FunctionSpec.Builder.addValueParameter(parameter: KotlinValueParameterSwiftModel) {
        addParameter(
            ParameterSpec.builder(parameter.argumentLabel, parameter.parameterName, parameter.type.toSwiftPoetUsage())
                .build()
        )
    }

    private fun FunctionSpec.Builder.addReturnType(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
            returns(bridgeModel.asyncOriginalFunction.returnType.toSwiftPoetUsage())
        }

    private fun FunctionSpec.Builder.addFunctionBody(bridgeModel: BridgeModel): FunctionSpec.Builder =
        this.apply {
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
                    .build()
            )
        }

    private val BridgeModel.argumentsForBridgingCall: CodeBlock
        get() {
            val arguments = mutableListOf<CodeBlock>()

            arguments.addDispatchReceiver(this)

            this.bridgedParameters.forEachIndexed { index, parameter ->
                if (isFromGenericClass) {
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

            add(CodeBlock.of("%N as! %T", bridgeModel.genericClassDispatchReceiverParameterName, dispatchReceiverErasedType))
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

    private fun TypeName.removingEscapingAttribute(): TypeName {
        return when (this) {
            is FunctionTypeName -> this.copy(attributes = this.attributes - AttributeSpec.ESCAPING)
            else -> this
        }
    }
}

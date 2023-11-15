package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.forEachAssociatedExportedSirDeclaration
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.applyToEntireOverrideHierarchy
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.sir.element.toSwiftVisibility
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
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
            val bridgeModel = BridgeModel(
                suspendKirFunction = kirProvider.getFunction(originalFunctionDescriptor),
                kotlinBridgingKirFunction = kirProvider.getFunction(kotlinBridgingFunctionDescriptor),
            )

            val extension = sirProvider.getExtension(
                classDeclaration = bridgeModel.extensionTypeDeclarationForBridgingFunction,
                parent = skieNamespaceProvider.getNamespaceFile(bridgeModel.suspendFunctionOwner),
            )

            markOriginalFunctionAsReplaced(bridgeModel.suspendKirFunction)

            bridgeModel.suspendKirFunction.bridgedSirFunction = extension.createSwiftBridgingFunction(bridgeModel)
        }
    }

    context(SirPhase.Context)
    private fun markOriginalFunctionAsReplaced(
        suspendKirFunction: KirSimpleFunction,
    ) {
        suspendKirFunction.forEachAssociatedExportedSirDeclaration {
            it.applyToEntireOverrideHierarchy {
                visibility = SirVisibility.PublicButReplaced
            }
        }
    }

    context(SirPhase.Context)
    private val BridgeModel.extensionTypeDeclarationForBridgingFunction: SirClass
        get() {
            return if (this.isFromGenericClass) {
                skieClassSuspendGenerator.getOrCreateSuspendClass(this.suspendFunctionOwner)
            } else {
                this.suspendFunctionOwner.originalSirClass
            }
        }
}

private fun SirExtension.createSwiftBridgingFunction(bridgeModel: BridgeModel): SirSimpleFunction =
    bridgeModel.originalFunction.shallowCopy(
        parent = this,
        isAsync = true,
        throws = true,
        visibility = bridgeModel.originalFunction.visibility.toSwiftVisibility(),
    ).apply {
        copyValueParametersFrom(bridgeModel.originalFunction)

        addSwiftBridgingFunctionBody(bridgeModel)
    }

private fun SirSimpleFunction.addSwiftBridgingFunctionBody(bridgeModel: BridgeModel) {
    addFunctionDeclarationBodyWithErrorTypeHandling(bridgeModel.kotlinBridgingFunction) {
        addCode(
            CodeBlock.builder()
                .addStatement("return try await SwiftCoroutineDispatcher.dispatch {")
                .indent()
                .apply {
                    addStatement(
                        "%T.%L",
                        bridgeModel.kotlinBridgingFunctionOwner.defaultType.evaluate().swiftPoetTypeName,
                        bridgeModel.kotlinBridgingFunction.call(bridgeModel.argumentsForBridgingCall),
                    )
                }
                .unindent()
                .addStatement("}")
                .build(),
        )
    }
}

private val BridgeModel.argumentsForBridgingCall: List<String>
    get() {
        val arguments = mutableListOf<String>()

        arguments.addDispatchReceiver(this)

        arguments.addValueParameters(this)

        arguments.addSuspendHandlerParameter()

        return arguments
    }

private fun MutableList<String>.addDispatchReceiver(bridgeModel: BridgeModel) {
    if (bridgeModel.originalFunction.scope != SirScope.Member) {
        return
    }

    val dispatchReceiverErasedType by lazy {
        bridgeModel.kotlinBridgingFunction.valueParameters.first().type.evaluate().swiftPoetTypeName
    }

    if (bridgeModel.isFromGenericClass) {
        add(CodeBlock.toString("%N as! %T", SkieClassSuspendGenerator.kotlinObjectVariableName, dispatchReceiverErasedType))
    } else if (bridgeModel.isFromBridgedClass) {
        add(CodeBlock.toString("self as %T", dispatchReceiverErasedType))
    } else {
        add("self")
    }
}

private fun MutableList<String>.addValueParameters(bridgeModel: BridgeModel) {
    bridgeModel.originalFunction.valueParameters.forEachIndexed { index, parameter ->
        if (bridgeModel.isFromGenericClass) {
            val erasedParameterType = bridgeModel.kotlinBridgingFunction.valueParameters[index + 1].type.evaluate().swiftPoetTypeName
                // Ideally we wouldn't need this, but in case the parameter is a lambda, it will have the escaping attribute which we can't use elsewhere.
                .removingEscapingAttribute()

            if (parameter.type.evaluate().swiftPoetTypeName != erasedParameterType) {
                add(CodeBlock.toString("%N as! %T", parameter.name, erasedParameterType))
            } else {
                add(CodeBlock.toString("%N", parameter.name))
            }
        } else {
            add(CodeBlock.toString("%N", parameter.name))
        }
    }
}

private fun MutableList<String>.addSuspendHandlerParameter() {
    add("$0")
}

private fun TypeName.removingEscapingAttribute(): TypeName {
    return when (this) {
        is FunctionTypeName -> this.copy(attributes = this.attributes - AttributeSpec.ESCAPING)
        else -> this
    }
}

private data class BridgeModel(
    val suspendKirFunction: KirSimpleFunction,
    val kotlinBridgingKirFunction: KirSimpleFunction,
) {

    val originalFunction: SirSimpleFunction =
        suspendKirFunction.bridgedSirFunction ?: error("Suspend function $suspendKirFunction does not have an async bridge.")

    val kotlinBridgingFunction: SirSimpleFunction = kotlinBridgingKirFunction.originalSirFunction

    val kotlinBridgingFunctionOwner: SirClass = kotlinBridgingKirFunction.owner.originalSirClass

    val suspendFunctionOwner: KirClass = suspendKirFunction.owner

    val isFromGenericClass: Boolean = this.suspendKirFunction.owner.typeParameters.isEmpty().not()

    // Can be called only during code generation
    val isFromBridgedClass: Boolean
        get() = suspendFunctionOwner.bridgedSirClass != null
}


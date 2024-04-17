package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.type.SupportedFlow
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.applyToEntireOverrideHierarchy
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.OirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.TypeName

class SwiftSuspendFunctionGenerator {

    private val skieClassSuspendGenerator = SkieClassSuspendGenerator()

    context(SirPhase.Context)
    fun generateSwiftBridgingFunction(
        suspendKirFunction: KirSimpleFunction,
        kotlinBridgingKirFunction: KirSimpleFunction,
    ) {
        val bridgeModel = SuspendFunctionBridgeModel(
            suspendKirFunction = suspendKirFunction,
            kotlinBridgingKirFunction = kotlinBridgingKirFunction,
        )

        val extension = sirProvider.getExtension(
            classDeclaration = bridgeModel.extensionTypeDeclarationForBridgingFunction,
            parent = namespaceProvider.getNamespaceFile(bridgeModel.suspendFunctionOwner),
        )

        bridgeModel.suspendKirFunction.bridgedSirFunction = extension.createSwiftBridgingFunction(bridgeModel)

        hideOriginalFunction(bridgeModel)
    }

    context(SirPhase.Context)
    private fun hideOriginalFunction(bridgeModel: SuspendFunctionBridgeModel) {
        bridgeModel.suspendKirFunctionAssociatedDeclarations.forEach {
            it.applyToEntireOverrideHierarchy {
                // Cannot use PublicButReplaced because the function might be annotated with @ShouldRefineInSwift
                if (visibility == SirVisibility.Public) {
                    visibility = SirVisibility.PublicButHidden
                }
                identifier = "__$identifier"
            }
        }
    }

    context(SirPhase.Context)
    private val SuspendFunctionBridgeModel.extensionTypeDeclarationForBridgingFunction: SirClass
        get() {
            return if (this.isFromGenericClass) {
                skieClassSuspendGenerator.getOrCreateSuspendClass(this.suspendFunctionOwner)
            } else {
                this.suspendFunctionOwner.originalSirClass
            }
        }
}

context(SirPhase.Context)
private fun SirExtension.createSwiftBridgingFunction(bridgeModel: SuspendFunctionBridgeModel): SirSimpleFunction =
    bridgeModel.originalFunction.shallowCopy(
        parent = this,
        isAsync = true,
        throws = true,
        returnType = bridgeModel.originalFunction.returnType.revertFlowMappingIfNeeded(),
    ).apply {
        copyValueParametersFrom(bridgeModel.originalFunction)

        addSwiftBridgingFunctionBody(bridgeModel)
    }

context(SirPhase.Context)
private fun SirType.revertFlowMappingIfNeeded(): SirType {
    when (this) {
        is OirDeclaredSirType -> {
            val flowVariant = SupportedFlow.allVariants.firstOrNull { declaration == it.getKotlinKirClass().oirClass } ?: return this

            return flowVariant.kind.getCoroutinesKirClass().originalSirClass.toType()
        }
        is NullableSirType -> return copy(type = type.revertFlowMappingIfNeeded())
        else -> return this
    }
}

private fun SirSimpleFunction.addSwiftBridgingFunctionBody(bridgeModel: SuspendFunctionBridgeModel) {
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

private val SuspendFunctionBridgeModel.argumentsForBridgingCall: List<String>
    get() {
        val arguments = mutableListOf<String>()

        arguments.addDispatchReceiver(this)

        arguments.addValueParameters(this)

        arguments.addSuspendHandlerParameter()

        return arguments
    }

private fun MutableList<String>.addDispatchReceiver(bridgeModel: SuspendFunctionBridgeModel) {
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

private fun MutableList<String>.addValueParameters(bridgeModel: SuspendFunctionBridgeModel) {
    bridgeModel.originalFunction.valueParameters.forEachIndexed { index, parameter ->
        if (bridgeModel.isFromGenericClass) {
            val erasedParameterType = bridgeModel.kotlinBridgingFunction.valueParameters[index + 1].type.evaluate().swiftPoetTypeName
                // Ideally we wouldn't need this, but in case the parameter is a lambda, it will have the escaping attribute which we can't use elsewhere.
                .removingEscapingAttribute()

            if (parameter.type.evaluate().swiftPoetTypeName != erasedParameterType) {
                add(CodeBlock.toString("%N as! %T", parameter.name, erasedParameterType))
            } else {
                add(parameter.name.escapeSwiftIdentifier())
            }
        } else {
            add(parameter.name.escapeSwiftIdentifier())
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


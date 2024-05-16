package co.touchlab.skie.phases.bridging

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.*
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.toNullable
import io.outfoxx.swiftpoet.CodeBlock

object ObjCBridgeableGenerator {

    /**
     * @param unwrapObjectiveCSource Whether to add `guard let` to crash on `nil` source in `fromObjectiveC`.
     */
    context(SirPhase.Context)
    fun addObjcBridgeableImplementation(
        target: SirClass,
        bridgedType: SirType,
        bridgeToObjectiveC: CodeBlock.Builder.() -> Unit,
        bridgeFromObjectiveC: CodeBlock.Builder.() -> Unit,
        unwrapObjectiveCSource: Boolean = true,
    ) = with(target) {
        addObjectiveCTypeAlias(bridgedType)
        addForceBridgeFromObjectiveC(bridgedType)
        addConditionallyBridgeFromObjectiveC(bridgedType)
        addUnconditionallyBridgeFromObjectiveC(bridgedType)
        addBridgeToObjectiveC(bridgedType, bridgeToObjectiveC)
        addFromObjectiveC(bridgedType, unwrapObjectiveCSource, bridgeFromObjectiveC)
    }

    private val SirClass.toTypeWithGenericParameters: SirDeclaredSirType
        get() = this.toType(this.typeParameters.map { it.toTypeParameterUsage() })

    context(SirPhase.Context)
    private fun SirClass.addObjectiveCTypeAlias(bridgedType: SirType) {
        SirTypeAlias(
            baseName = sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first().name,
        ) {
            bridgedType
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addForceBridgeFromObjectiveC(bridgedType: SirType) {
        SirSimpleFunction(
            identifier = "_forceBridgeFromObjectiveC",
            returnType = sirBuiltins.Swift.Void.defaultType,
            scope = SirScope.Static,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "source",
                type = bridgedType,
            )

            SirValueParameter(
                name = "result",
                type = toTypeWithGenericParameters.toNullable(),
                inout = true,
            )

            bodyBuilder.add {
                addStatement("result = fromObjectiveC(source)")
            }
        }
    }

    context(SirPhase.Context)
    private fun SirClass.addConditionallyBridgeFromObjectiveC(bridgedType: SirType) {
        SirSimpleFunction(
            identifier = "_conditionallyBridgeFromObjectiveC",
            returnType = sirBuiltins.Swift.Bool.defaultType,
            scope = SirScope.Static,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "source",
                type = bridgedType,
            )

            SirValueParameter(
                name = "result",
                type = toTypeWithGenericParameters.toNullable(),
                inout = true,
            )

            bodyBuilder.add {
                addStatement("result = fromObjectiveC(source)")
                addStatement("return true")
            }
        }
    }

    private fun SirClass.addUnconditionallyBridgeFromObjectiveC(bridgedType: SirType) {
        SirSimpleFunction(
            identifier = "_unconditionallyBridgeFromObjectiveC",
            returnType = SpecialSirType.Self,
            scope = SirScope.Static,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "source",
                type = bridgedType.toNullable(),
            )

            bodyBuilder.add {
                addStatement("return fromObjectiveC(source)")
            }
        }
    }

    private fun SirClass.addBridgeToObjectiveC(bridgedType: SirType, body: CodeBlock.Builder.() -> Unit) {
        SirSimpleFunction(
            identifier = "_bridgeToObjectiveC",
            returnType = bridgedType,
        ).apply {
            bodyBuilder.add {
                addCode(CodeBlock.Builder().apply(body).build())
            }
        }
    }

    private fun SirClass.addFromObjectiveC(
        bridgedType: SirType,
        unwrapObjectiveCSource: Boolean,
        body: CodeBlock.Builder.() -> Unit,
    ) {
        SirSimpleFunction(
            identifier = "fromObjectiveC",
            returnType = SpecialSirType.Self,
            scope = SirScope.Static,
            visibility = SirVisibility.Private,
        ).apply {
            SirValueParameter(
                label = "_",
                name = "source",
                type = bridgedType.toNullable(),
            )

            bodyBuilder.add {
                if (unwrapObjectiveCSource) {
                    beginControlFlow("guard", "let source = source else")
                    addCode(fatalErrorFromObjectiveC(this@addFromObjectiveC))
                    endControlFlow("guard")
                }
                addCode(CodeBlock.Builder().apply(body).build())
            }
        }
    }

    fun fatalErrorFromObjectiveC(sirClass: SirClass): CodeBlock =
        CodeBlock.of("""fatalError("Couldn't map value of \(Swift.String(describing: source)) to ${sirClass.publicName}")""")
}

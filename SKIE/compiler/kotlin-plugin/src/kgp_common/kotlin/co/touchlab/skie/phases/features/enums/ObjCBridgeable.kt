package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.optional
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.swiftmodel.type.enumentry.KotlinEnumEntrySwiftModel
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.joinToCode

context(SirPhase.Context)
fun SirClass.addObjcBridgeableImplementation(swiftModel: KotlinClassSwiftModel) {
    addObjectiveCTypeAlias(swiftModel)
    addForceBridgeFromObjectiveC(swiftModel)
    addConditionallyBridgeFromObjectiveC(swiftModel)
    addUnconditionallyBridgeFromObjectiveC(swiftModel)
    addBridgeToObjectiveC(swiftModel)
    addFromObjectiveC(swiftModel)
}

context(SirPhase.Context)
private fun SirClass.addObjectiveCTypeAlias(swiftModel: KotlinClassSwiftModel) {
    SirTypeAlias(
        simpleName = sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first().name,
    ) {
        swiftModel.kotlinSirClass.defaultType
    }
}

context(SirPhase.Context)
private fun SirClass.addForceBridgeFromObjectiveC(swiftModel: KotlinClassSwiftModel) {
    SirFunction(
        identifier = "_forceBridgeFromObjectiveC",
        returnType = sirBuiltins.Swift.Void.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = swiftModel.kotlinSirClass.defaultType,
        )

        SirValueParameter(
            name = "result",
            type = SpecialSirType.Self.optional(),
            inout = true,
        )

        swiftPoetBuilderModifications.add {
            addStatement("result = fromObjectiveC(source)")
        }
    }
}

context(SirPhase.Context)
private fun SirClass.addConditionallyBridgeFromObjectiveC(swiftModel: KotlinClassSwiftModel) {
    SirFunction(
        identifier = "_conditionallyBridgeFromObjectiveC",
        returnType = sirBuiltins.Swift.Bool.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = swiftModel.kotlinSirClass.defaultType,
        )

        SirValueParameter(
            name = "result",
            type = SpecialSirType.Self.optional(),
            inout = true,
        )

        swiftPoetBuilderModifications.add {
            addStatement("result = fromObjectiveC(source)")
            addStatement("return true")
        }
    }
}

private fun SirClass.addUnconditionallyBridgeFromObjectiveC(swiftModel: KotlinClassSwiftModel) {
    SirFunction(
        identifier = "_unconditionallyBridgeFromObjectiveC",
        returnType = SpecialSirType.Self,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = swiftModel.kotlinSirClass.defaultType.optional(),
        )

        swiftPoetBuilderModifications.add {
            addStatement("return fromObjectiveC(source)")
        }
    }
}

private fun SirClass.addBridgeToObjectiveC(swiftModel: KotlinClassSwiftModel) {
    SirFunction(
        identifier = "_bridgeToObjectiveC",
        returnType = swiftModel.kotlinSirClass.defaultType,
    ).apply {
        addBridgeToObjectiveCBody(swiftModel)
    }
}

private fun SirFunction.addBridgeToObjectiveCBody(swiftModel: KotlinClassSwiftModel) {
    swiftPoetBuilderModifications.add {
        addCode(
            CodeBlock.builder()
                .beginControlFlow("switch", "self")
                .add(
                    swiftModel.enumEntries.map { it.swiftBridgeCase }.joinToCode("\n", suffix = "\n"),
                )
                .endControlFlow("switch")
                .build(),
        )
    }
}

private val KotlinEnumEntrySwiftModel.swiftBridgeCase: CodeBlock
    get() = CodeBlock.of(
        "case .%N: return %T.%N as %T",
        identifier,
        enum.kotlinSirClass.defaultType.toSwiftPoetTypeName(),
        identifier,
        enum.kotlinSirClass.defaultType.toSwiftPoetTypeName(),
    )

private fun SirClass.addFromObjectiveC(swiftModel: KotlinClassSwiftModel) {
    SirFunction(
        identifier = "fromObjectiveC",
        returnType = SpecialSirType.Self,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = swiftModel.kotlinSirClass.defaultType.optional(),
        )

        addFromObjectiveCBody(swiftModel)
    }
}

private fun SirFunction.addFromObjectiveCBody(swiftModel: KotlinClassSwiftModel) {
    swiftPoetBuilderModifications.add {
        addCode(
            CodeBlock.builder()
                .apply {
                    addNonEmptyFromObjectiveCBody(swiftModel)
                }
                .build(),
        )
    }
}

private fun CodeBlock.Builder.addNonEmptyFromObjectiveCBody(
    swiftModel: KotlinClassSwiftModel,
) {
    if (swiftModel.enumEntries.isNotEmpty()) {
        addStatement("guard let source = source else { %L }", swiftModel.fatalErrorFromObjectiveC)

        swiftModel.enumEntries.forEachIndexed { index, entry ->
            addFromObjectiveCBodyCase(entry, index)
        }

        nextControlFlow("else")
        addStatement("%L", swiftModel.fatalErrorFromObjectiveC)
        endControlFlow("if")
    } else {
        add(swiftModel.fatalErrorFromObjectiveC)
    }
}

private fun CodeBlock.Builder.addFromObjectiveCBodyCase(
    entry: KotlinEnumEntrySwiftModel,
    index: Int,
) {
    val controlFlowCode = "source == %T.%N as %T"
    val controlFlowArguments = arrayOf(
        entry.enum.kotlinSirClass.defaultType.toSwiftPoetTypeName(),
        entry.identifier,
        entry.enum.kotlinSirClass.defaultType.toSwiftPoetTypeName(),
    )

    if (index == 0) {
        beginControlFlow("if", controlFlowCode, *controlFlowArguments)
    } else {
        nextControlFlow("else if", controlFlowCode, *controlFlowArguments)
    }

    addStatement("return .%N", entry.identifier)
}

private val KotlinClassSwiftModel.fatalErrorFromObjectiveC: CodeBlock
    get() = CodeBlock.of("""fatalError("Couldn't map value of \(Swift.String(describing: source)) to ${kotlinSirClass.fqName}")""")

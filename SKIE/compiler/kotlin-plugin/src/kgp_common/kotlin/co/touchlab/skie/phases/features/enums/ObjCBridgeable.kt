package co.touchlab.skie.phases.features.enums

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirScope
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.toNullable
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.joinToCode

context(SirPhase.Context)
fun SirClass.addObjcBridgeableImplementation(enumKirClass: KirClass) {
    addObjectiveCTypeAlias(enumKirClass.enum)
    addForceBridgeFromObjectiveC(enumKirClass.enum)
    addConditionallyBridgeFromObjectiveC(enumKirClass.enum)
    addUnconditionallyBridgeFromObjectiveC(enumKirClass.enum)
    addBridgeToObjectiveC(enumKirClass)
    addFromObjectiveC(enumKirClass)
}

private val KirClass.enum: SirClass
    get() = this.originalSirClass

context(SirPhase.Context)
private fun SirClass.addObjectiveCTypeAlias(enum: SirClass) {
    SirTypeAlias(
        baseName = sirBuiltins.Swift._ObjectiveCBridgeable.typeParameters.first().name,
    ) {
        enum.defaultType
    }
}

context(SirPhase.Context)
private fun SirClass.addForceBridgeFromObjectiveC(enum: SirClass) {
    SirSimpleFunction(
        identifier = "_forceBridgeFromObjectiveC",
        returnType = sirBuiltins.Swift.Void.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = enum.defaultType,
        )

        SirValueParameter(
            name = "result",
            type = SpecialSirType.Self.toNullable(),
            inout = true,
        )

        swiftPoetBuilderModifications.add {
            addStatement("result = fromObjectiveC(source)")
        }
    }
}

context(SirPhase.Context)
private fun SirClass.addConditionallyBridgeFromObjectiveC(enum: SirClass) {
    SirSimpleFunction(
        identifier = "_conditionallyBridgeFromObjectiveC",
        returnType = sirBuiltins.Swift.Bool.defaultType,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = enum.defaultType,
        )

        SirValueParameter(
            name = "result",
            type = SpecialSirType.Self.toNullable(),
            inout = true,
        )

        swiftPoetBuilderModifications.add {
            addStatement("result = fromObjectiveC(source)")
            addStatement("return true")
        }
    }
}

private fun SirClass.addUnconditionallyBridgeFromObjectiveC(enum: SirClass) {
    SirSimpleFunction(
        identifier = "_unconditionallyBridgeFromObjectiveC",
        returnType = SpecialSirType.Self,
        scope = SirScope.Static,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = enum.defaultType.toNullable(),
        )

        swiftPoetBuilderModifications.add {
            addStatement("return fromObjectiveC(source)")
        }
    }
}

private fun SirClass.addBridgeToObjectiveC(enumKirClass: KirClass) {
    SirSimpleFunction(
        identifier = "_bridgeToObjectiveC",
        returnType = enumKirClass.enum.defaultType,
    ).apply {
        addBridgeToObjectiveCBody(enumKirClass)
    }
}

private fun SirSimpleFunction.addBridgeToObjectiveCBody(enumKirClass: KirClass) {
    swiftPoetBuilderModifications.add {
        addCode(
            CodeBlock.builder()
                .beginControlFlow("switch", "self")
                .add(
                    enumKirClass.enumEntries.map { it.getSwiftBridgeCase(enumKirClass.enum) }.joinToCode("\n", suffix = "\n"),
                )
                .endControlFlow("switch")
                .build(),
        )
    }
}

private fun KirEnumEntry.getSwiftBridgeCase(enum: SirClass): CodeBlock {
    val typeName = enum.defaultType.evaluate().swiftPoetTypeName

    return CodeBlock.of(
        "case .%N: return %T.%N as %T",
        swiftName,
        typeName,
        swiftName,
        typeName,
    )
}

private fun SirClass.addFromObjectiveC(enumKirClass: KirClass) {
    SirSimpleFunction(
        identifier = "fromObjectiveC",
        returnType = SpecialSirType.Self,
        scope = SirScope.Static,
        visibility = SirVisibility.Private,
    ).apply {
        SirValueParameter(
            label = "_",
            name = "source",
            type = enumKirClass.enum.defaultType.toNullable(),
        )

        addFromObjectiveCBody(enumKirClass)
    }
}

private fun SirSimpleFunction.addFromObjectiveCBody(enumKirClass: KirClass) {
    swiftPoetBuilderModifications.add {
        addCode(
            CodeBlock.builder()
                .apply {
                    addNonEmptyFromObjectiveCBody(enumKirClass)
                }
                .build(),
        )
    }
}

private fun CodeBlock.Builder.addNonEmptyFromObjectiveCBody(
    enumKirClass: KirClass,
) {
    val enum = enumKirClass.enum

    if (enumKirClass.enumEntries.isNotEmpty()) {
        addStatement("guard let source = source else { %L }", enum.fatalErrorFromObjectiveC)

        enumKirClass.enumEntries.forEachIndexed { index, entry ->
            addFromObjectiveCBodyCase(entry, enum, index)
        }

        nextControlFlow("else")
        addStatement("%L", enum.fatalErrorFromObjectiveC)
        endControlFlow("if")
    } else {
        add(enum.fatalErrorFromObjectiveC)
    }
}

private fun CodeBlock.Builder.addFromObjectiveCBodyCase(
    entry: KirEnumEntry,
    enum: SirClass,
    index: Int,
) {
    val typeName = enum.defaultType.evaluate().swiftPoetTypeName

    val controlFlowCode = "source == %T.%N as %T"
    val controlFlowArguments = arrayOf(typeName, entry.swiftName, typeName)

    if (index == 0) {
        beginControlFlow("if", controlFlowCode, *controlFlowArguments)
    } else {
        nextControlFlow("else if", controlFlowCode, *controlFlowArguments)
    }

    addStatement("return .%N", entry.swiftName)
}

private val SirClass.fatalErrorFromObjectiveC: CodeBlock
    get() = CodeBlock.of("""fatalError("Couldn't map value of \(Swift.String(describing: source)) to $publicName")""")

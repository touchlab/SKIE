package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.api.model.type.stableSpec
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode

internal object ObjectiveCBridgeable {

    fun TypeSpec.Builder.addObjcBridgeableImplementation(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this
            .addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))
            .addObjectiveCTypeAlias(classSwiftModel)
            .addForceBridgeFromObjectiveC(classSwiftModel)
            .addConditionallyBridgeFromObjectiveC(classSwiftModel)
            .addUnconditionallyBridgeFromObjectiveC(classSwiftModel)
            .addBridgeToObjectiveC(classSwiftModel)
            .addFromObjectiveC(classSwiftModel)

    private fun TypeSpec.Builder.addObjectiveCTypeAlias(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addType(
            TypeAliasSpec.builder("_ObjectiveCType", classSwiftModel.stableSpec)
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

    private fun TypeSpec.Builder.addForceBridgeFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_forceBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.stableSpec)
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .build()
        )

    private fun TypeSpec.Builder.addConditionallyBridgeFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.stableSpec)
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .addStatement("return true")
                .returns(BOOL)
                .build()
        )

    private fun TypeSpec.Builder.addUnconditionallyBridgeFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.stableSpec.makeOptional())
                .addStatement("return fromObjectiveC(source)")
                .returns(SelfTypeName.INSTANCE)
                .build()
        )

    private fun TypeSpec.Builder.addBridgeToObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_bridgeToObjectiveC")
                .addModifiers(Modifier.PUBLIC)
                .returns(classSwiftModel.stableSpec)
                .addBridgeToObjectiveCBody(classSwiftModel)
                .build()
        )

    private fun FunctionSpec.Builder.addBridgeToObjectiveCBody(classSwiftModel: KotlinClassSwiftModel): FunctionSpec.Builder =
        this.addCode(
            CodeBlock.builder()
                .beginControlFlow("switch", "self")
                .add(
                    classSwiftModel.enumEntries.map { it.swiftBridgeCase }.joinToCode("\n", suffix = "\n")
                )
                .endControlFlow("switch")
                .build()
        )

    private val KotlinEnumEntrySwiftModel.swiftBridgeCase: CodeBlock
        get() = CodeBlock.of(
            "case .%N: return %T.%N as %T",
            this.identifier,
            this.enum.stableSpec,
            this.identifier,
            this.enum.stableSpec,
        )

    private fun TypeSpec.Builder.addFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("fromObjectiveC")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.stableSpec.makeOptional())
                .returns(SelfTypeName.INSTANCE)
                .addFromObjectiveCBody(classSwiftModel)
                .build()
        )

    private fun FunctionSpec.Builder.addFromObjectiveCBody(classSwiftModel: KotlinClassSwiftModel): FunctionSpec.Builder =
        this.addCode(
            CodeBlock.builder()
                .addFromObjectiveCEntriesVariables(classSwiftModel)
                .beginControlFlow("switch", "source")
                .addFromObjectiveCCases(classSwiftModel)
                .addStatement("default: fatalError(\"Couldn't map value of \\(String(describing: source)) to ${classSwiftModel.identifier}\")")
                .endControlFlow("switch")
                .build()
        )

    private fun CodeBlock.Builder.addFromObjectiveCEntriesVariables(classSwiftModel: KotlinClassSwiftModel): CodeBlock.Builder =
        this.add(
            classSwiftModel.enumEntries.map { it.variableWithEntryCastedToSwiftType }.joinToCode("\n", suffix = "\n")
        )

    private val KotlinEnumEntrySwiftModel.variableWithEntryCastedToSwiftType: CodeBlock
        get() = CodeBlock.of("let objc__%N = %T.%N as %T", this.identifier, this.enum.stableSpec, this.identifier, this.enum.stableSpec)

    private fun CodeBlock.Builder.addFromObjectiveCCases(classSwiftModel: KotlinClassSwiftModel): CodeBlock.Builder =
        this.add(
            classSwiftModel.enumEntries.map { it.objectiveCBridgeCase }.joinToCode("\n", suffix = "\n")
        )

    private val KotlinEnumEntrySwiftModel.objectiveCBridgeCase: CodeBlock
        get() = CodeBlock.of("case objc__%N?: return .%N", this.identifier, this.identifier)
}

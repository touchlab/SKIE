package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode

internal object ObjCBridgeable {

    val type: DeclaredTypeName = DeclaredTypeName("Swift", "_ObjectiveCBridgeable")
    val bridgedObjCTypeAlias: String = "_ObjectiveCType"

    fun TypeSpec.Builder.addObjcBridgeableImplementation(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this
            .addSuperType(type)
            .addObjectiveCTypeAlias(classSwiftModel)
            .addForceBridgeFromObjectiveC(classSwiftModel)
            .addConditionallyBridgeFromObjectiveC(classSwiftModel)
            .addUnconditionallyBridgeFromObjectiveC(classSwiftModel)
            .addBridgeToObjectiveC(classSwiftModel)
            .addFromObjectiveC(classSwiftModel)

    private fun TypeSpec.Builder.addObjectiveCTypeAlias(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addType(
            TypeAliasSpec.builder(bridgedObjCTypeAlias, classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

    private fun TypeSpec.Builder.addForceBridgeFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_forceBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .build()
        )

    private fun TypeSpec.Builder.addConditionallyBridgeFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
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
                .addParameter("_", "source", classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName().makeOptional())
                .addStatement("return fromObjectiveC(source)")
                .returns(SelfTypeName.INSTANCE)
                .build()
        )

    private fun TypeSpec.Builder.addBridgeToObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("_bridgeToObjectiveC")
                .addModifiers(Modifier.PUBLIC)
                .returns(classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName())
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
            this.enum.nonBridgedDeclaration.internalName.toSwiftPoetName(),
            this.identifier,
            this.enum.nonBridgedDeclaration.internalName.toSwiftPoetName(),
        )

    private fun TypeSpec.Builder.addFromObjectiveC(classSwiftModel: KotlinClassSwiftModel): TypeSpec.Builder =
        this.addFunction(
            FunctionSpec.builder("fromObjectiveC")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter("_", "source", classSwiftModel.nonBridgedDeclaration.internalName.toSwiftPoetName().makeOptional())
                .returns(SelfTypeName.INSTANCE)
                .addFromObjectiveCBody(classSwiftModel)
                .build()
        )

    private fun FunctionSpec.Builder.addFromObjectiveCBody(classSwiftModel: KotlinClassSwiftModel): FunctionSpec.Builder =
        this.addCode(
            CodeBlock.builder()
                .apply {
                    if (classSwiftModel.enumEntries.isNotEmpty()) {
                        addStatement("guard let source = source else { %L }", classSwiftModel.fatalErrorFromObjectiveC)
                        classSwiftModel.enumEntries.forEachIndexed { index, entry ->
                            val controlFlowCode = "source == %T.%N as %T"
                            val controlFlowArguments = arrayOf(
                                entry.enum.nonBridgedDeclaration.internalName.toSwiftPoetName(),
                                entry.identifier,
                                entry.enum.nonBridgedDeclaration.internalName.toSwiftPoetName(),
                            )
                            if (index == 0) {
                                beginControlFlow("if", controlFlowCode, *controlFlowArguments)
                            } else {
                                nextControlFlow("else if", controlFlowCode, *controlFlowArguments)
                            }
                            addStatement("return .%N", entry.identifier)
                        }
                        nextControlFlow("else")
                        addStatement("%L", classSwiftModel.fatalErrorFromObjectiveC)
                        endControlFlow("if")
                    } else {
                        addCode(classSwiftModel.fatalErrorFromObjectiveC)
                    }
                }
                .build()
        )

    private val KotlinClassSwiftModel.fatalErrorFromNil: CodeBlock
        get() = CodeBlock.of("""fatalError("Couldn't map nil to $identifier")""")

    private val KotlinClassSwiftModel.fatalErrorFromObjectiveC: CodeBlock
        get() = CodeBlock.of("""fatalError("Couldn't map value of \(String(describing: source)) to $identifier")""")
}

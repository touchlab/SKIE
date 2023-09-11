package co.touchlab.skie.plugin.generator.internal.enums

import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode

object ObjCBridgeable {

    val bridgedObjCTypeAlias: String = "_ObjectiveCType"
}

context(KotlinClassSwiftModel)
fun TypeSpec.Builder.addObjcBridgeableImplementation(): TypeSpec.Builder =
    this
        .addObjectiveCTypeAlias()
        .addForceBridgeFromObjectiveC()
        .addConditionallyBridgeFromObjectiveC()
        .addUnconditionallyBridgeFromObjectiveC()
        .addBridgeToObjectiveC()
        .addFromObjectiveC()

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addObjectiveCTypeAlias(): TypeSpec.Builder =
    addType(
        TypeAliasSpec.builder(ObjCBridgeable.bridgedObjCTypeAlias, kotlinSirClass.internalName.toSwiftPoetName())
            .addModifiers(Modifier.PUBLIC)
            .build()
    )

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addForceBridgeFromObjectiveC(): TypeSpec.Builder =
    addFunction(
        FunctionSpec.builder("_forceBridgeFromObjectiveC")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter("_", "source", kotlinSirClass.internalName.toSwiftPoetName())
            .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
            .addStatement("result = fromObjectiveC(source)")
            .build()
    )

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addConditionallyBridgeFromObjectiveC(): TypeSpec.Builder =
    addFunction(
        FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter("_", "source", kotlinSirClass.internalName.toSwiftPoetName())
            .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
            .addStatement("result = fromObjectiveC(source)")
            .addStatement("return true")
            .returns(BOOL)
            .build()
    )

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addUnconditionallyBridgeFromObjectiveC(): TypeSpec.Builder =
    addFunction(
        FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter("_", "source", kotlinSirClass.internalName.toSwiftPoetName().makeOptional())
            .addStatement("return fromObjectiveC(source)")
            .returns(SelfTypeName.INSTANCE)
            .build()
    )

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addBridgeToObjectiveC(): TypeSpec.Builder =
    addFunction(
        FunctionSpec.builder("_bridgeToObjectiveC")
            .addModifiers(Modifier.PUBLIC)
            .returns(kotlinSirClass.internalName.toSwiftPoetName())
            .addBridgeToObjectiveCBody()
            .build()
    )

context(KotlinClassSwiftModel)
private fun FunctionSpec.Builder.addBridgeToObjectiveCBody(): FunctionSpec.Builder =
    addCode(
        CodeBlock.builder()
            .beginControlFlow("switch", "self")
            .add(
                enumEntries.map { it.swiftBridgeCase }.joinToCode("\n", suffix = "\n")
            )
            .endControlFlow("switch")
            .build()
    )

private val KotlinEnumEntrySwiftModel.swiftBridgeCase: CodeBlock
    get() = CodeBlock.of(
        "case .%N: return %T.%N as %T",
        identifier,
        enum.kotlinSirClass.internalName.toSwiftPoetName(),
        identifier,
        enum.kotlinSirClass.internalName.toSwiftPoetName(),
    )

context(KotlinClassSwiftModel)
private fun TypeSpec.Builder.addFromObjectiveC(): TypeSpec.Builder =
    addFunction(
        FunctionSpec.builder("fromObjectiveC")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter("_", "source", kotlinSirClass.internalName.toSwiftPoetName().makeOptional())
            .returns(SelfTypeName.INSTANCE)
            .addFromObjectiveCBody()
            .build()
    )

context(KotlinClassSwiftModel)
private fun FunctionSpec.Builder.addFromObjectiveCBody(): FunctionSpec.Builder =
    addCode(
        CodeBlock.builder()
            .apply {
                if (enumEntries.isNotEmpty()) {
                    addStatement("guard let source = source else { %L }", fatalErrorFromObjectiveC)
                    enumEntries.forEachIndexed { index, entry ->
                        val controlFlowCode = "source == %T.%N as %T"
                        val controlFlowArguments = arrayOf(
                            entry.enum.kotlinSirClass.internalName.toSwiftPoetName(),
                            entry.identifier,
                            entry.enum.kotlinSirClass.internalName.toSwiftPoetName(),
                        )
                        if (index == 0) {
                            beginControlFlow("if", controlFlowCode, *controlFlowArguments)
                        } else {
                            nextControlFlow("else if", controlFlowCode, *controlFlowArguments)
                        }
                        addStatement("return .%N", entry.identifier)
                    }
                    nextControlFlow("else")
                    addStatement("%L", fatalErrorFromObjectiveC)
                    endControlFlow("if")
                } else {
                    addCode(fatalErrorFromObjectiveC)
                }
            }
            .build()
    )

private val KotlinClassSwiftModel.fatalErrorFromObjectiveC: CodeBlock
    get() = CodeBlock.of("""fatalError("Couldn't map value of \(String(describing: source)) to ${kotlinSirClass.fqName}")""")

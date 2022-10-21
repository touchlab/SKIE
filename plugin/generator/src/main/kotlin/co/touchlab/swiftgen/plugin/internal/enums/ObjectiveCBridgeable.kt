package co.touchlab.swiftgen.plugin.internal.enums

import co.touchlab.swiftpack.api.SwiftPoetScope
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal object ObjectiveCBridgeable {
    context(SwiftPoetScope)
    fun TypeSpec.Builder.addObjcBridgeableImplementation(
        declaration: ClassDescriptor,
    ) {
        addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))

        addType(
            TypeAliasSpec.builder("_ObjectiveCType", declaration.spec)
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        addBridgeToObjectiveC(declaration)

        addForceBridgeFromObjectiveC(declaration)

        addConditionallyBridgeFromObjectiveC(declaration)

        addUnconditionallyBridgeFromObjectiveC(declaration)

        addFromObjectiveC(declaration)
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addBridgeToObjectiveC(
        declaration: ClassDescriptor,
    ) {
        addFunction(
            FunctionSpec.builder("_bridgeToObjectiveC")
                .addModifiers(Modifier.PUBLIC)
                .returns(declaration.spec)
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("switch", "self")
                        .add(
                            declaration.enumEntries.map {
                                CodeBlock.of(
                                    "case .%N: return %T.%N",
                                    it.swiftName.simpleName,
                                    declaration.spec,
                                    it.swiftName.simpleName,
                                )
                            }.joinToCode("\n")
                        )
                        .endControlFlow("switch")
                        .build()
                )
                .build()
        )
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addForceBridgeFromObjectiveC(declaration: ClassDescriptor) {
        addFunction(
            FunctionSpec.builder("_forceBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declaration.spec)
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .build()
        )
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addConditionallyBridgeFromObjectiveC(declaration: ClassDescriptor) {
        addFunction(
            FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declaration.spec)
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .addStatement("return true")
                .returns(BOOL)
                .build()
        )
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addUnconditionallyBridgeFromObjectiveC(declaration: ClassDescriptor) {
        addFunction(
            FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declaration.spec.makeOptional())
                .addStatement("return fromObjectiveC(source)")
                .returns(SelfTypeName.INSTANCE)
                .build()
        )
    }

    context(SwiftPoetScope)
    private fun TypeSpec.Builder.addFromObjectiveC(
        declaration: ClassDescriptor,
    ) {
        addFunction(
            FunctionSpec.builder("fromObjectiveC")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter("_", "source", declaration.spec.makeOptional())
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("switch", "source")
                        .add(
                            declaration.enumEntries.map {
                                CodeBlock.of(
                                    "case .%N?: return .%N",
                                    it.swiftName.simpleName,
                                    it.swiftName.simpleName,
                                )
                            }.joinToCode("\n", suffix = "\n")
                        )
                        .addStatement("default: fatalError(\"Couldn't map value of \\(String(describing: source)) to ${declaration.name.asString()}\")")
                        .endControlFlow("switch")
                        .build()
                )
                .returns(SelfTypeName.INSTANCE)
                .build()
        )
    }


}

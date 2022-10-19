package co.touchlab.swiftgen.plugin.internal.enums

import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.spec.reference.KotlinClassReference
import co.touchlab.swiftpack.spec.reference.KotlinEnumEntryReference
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal object ObjectiveCBridgeable {
    context(SwiftPackModuleBuilder)
    fun TypeSpec.Builder.addObjcBridgeableImplementation(
        declaration: ClassDescriptor,
        declarationReference: KotlinClassReference,
        declaredCases: List<KotlinEnumEntryReference>,
    ) {
        addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))

        addType(
            TypeAliasSpec.builder("_ObjectiveCType", declarationReference.swiftTemplateVariable())
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        addBridgeToObjectiveC(declarationReference, declaredCases)

        addForceBridgeFromObjectiveC(declarationReference)

        addConditionallyBridgeFromObjectiveC(declarationReference)

        addUnconditionallyBridgeFromObjectiveC(declarationReference)

        addFromObjectiveC(declarationReference, declaredCases, declaration)
    }

    context(SwiftPackModuleBuilder)
    private fun TypeSpec.Builder.addFromObjectiveC(
        declarationReference: KotlinClassReference,
        declaredCases: List<KotlinEnumEntryReference>,
        declaration: ClassDescriptor,
    ) {
        addFunction(
            FunctionSpec.builder("fromObjectiveC")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter("_", "source", declarationReference.swiftTemplateVariable().makeOptional())
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("switch", "source")
                        .add(
                            declaredCases.map {
                                CodeBlock.of(
                                    "case .%N?: return .%N",
                                    it.swiftTemplateVariable(),
                                    it.swiftTemplateVariable()
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

    context(SwiftPackModuleBuilder)
    private fun TypeSpec.Builder.addUnconditionallyBridgeFromObjectiveC(declarationReference: KotlinClassReference) {
        addFunction(
            FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declarationReference.swiftTemplateVariable().makeOptional())
                .addStatement("return fromObjectiveC(source)")
                .returns(SelfTypeName.INSTANCE)
                .build()
        )
    }

    context(SwiftPackModuleBuilder)
    private fun TypeSpec.Builder.addConditionallyBridgeFromObjectiveC(declarationReference: KotlinClassReference) {
        addFunction(
            FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declarationReference.swiftTemplateVariable())
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .addStatement("return true")
                .returns(BOOL)
                .build()
        )
    }

    context(SwiftPackModuleBuilder)
    private fun TypeSpec.Builder.addForceBridgeFromObjectiveC(declarationReference: KotlinClassReference) {
        addFunction(
            FunctionSpec.builder("_forceBridgeFromObjectiveC")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter("_", "source", declarationReference.swiftTemplateVariable())
                .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                .addStatement("result = fromObjectiveC(source)")
                .build()
        )
    }

    context(SwiftPackModuleBuilder)
    private fun TypeSpec.Builder.addBridgeToObjectiveC(
        declarationReference: KotlinClassReference,
        declaredCases: List<KotlinEnumEntryReference>,
    ) {
        addFunction(
            FunctionSpec.builder("_bridgeToObjectiveC")
                .addModifiers(Modifier.PUBLIC)
                .returns(declarationReference.swiftTemplateVariable())
                .addCode(
                    CodeBlock.builder()
                        .beginControlFlow("switch", "self")
                        .add(
                            declaredCases.map {
                                CodeBlock.of(
                                    "case .%N: return %T.%N",
                                    it.swiftTemplateVariable(),
                                    declarationReference.swiftTemplateVariable(),
                                    it.swiftTemplateVariable()
                                )
                            }.joinToCode("\n")
                        )
                        .endControlFlow("switch")
                        .build()
                )
                .build()
        )
    }


}

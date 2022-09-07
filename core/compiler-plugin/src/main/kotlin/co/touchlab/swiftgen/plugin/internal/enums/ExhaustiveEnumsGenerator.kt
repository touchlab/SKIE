package co.touchlab.swiftgen.plugin.internal.enums

import co.touchlab.swiftgen.plugin.internal.util.BaseGenerator
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.IrWalker
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.SelfTypeName
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.joinToCode
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

internal class ExhaustiveEnumsGenerator(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    private val swiftPackModuleBuilder: SwiftPackModuleBuilder,
) : BaseGenerator(fileBuilderFactory, namespaceProvider) {

    override fun generate(module: IrModuleFragment) {
        module.acceptChildrenVoid(Walker())
    }

    private inner class Walker : IrWalker {

        override fun visitClass(declaration: IrClass) {
            super.visitClass(declaration)

            generate(declaration)
        }
    }

    private fun generate(declaration: IrClass) {
        if (!shouldGenerateExhaustiveEnums(declaration)) {
            return
        }

        generateCode(declaration) {
            with(swiftPackModuleBuilder) {
                val declarationReference = declaration.reference()
                declarationReference.applyTransform {
                    hide()
                    bridge(declarationReference.typeName)
                }

                val declaredCases = declaration.declarations.filterIsInstance<IrEnumEntry>().map { it.reference() }
                addType(
                    TypeSpec.enumBuilder(declarationReference.typeName)
                        .apply {
                            addAttribute("frozen")
                            addModifiers(Modifier.PUBLIC)
                            addSuperType(DeclaredTypeName("Swift", "_ObjectiveCBridgeable"))

                            declaredCases.forEach {
                                addEnumCase(it.swiftReference().name)
                            }

                            addType(
                                TypeAliasSpec.builder("_ObjectiveCType", declarationReference.swiftReference())
                                    .addModifiers(Modifier.PUBLIC)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_bridgeToObjectiveC")
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(declarationReference.swiftReference())
                                    .addCode(
                                        CodeBlock.builder()
                                            .beginControlFlow("switch", "self")
                                            .add(
                                                declaredCases.map {
                                                    CodeBlock.of(
                                                        "case .%N: return %T.%N",
                                                        it.swiftReference(),
                                                        declarationReference.swiftReference(),
                                                        it.swiftReference()
                                                    )
                                                }.joinToCode("\n")
                                            )
                                            .endControlFlow("switch")
                                            .build()
                                    )
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_forceBridgeFromObjectiveC")
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference())
                                    .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                    .addStatement("result = fromObjectiveC(source)")
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_conditionallyBridgeFromObjectiveC")
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference())
                                    .addParameter("result", SelfTypeName.INSTANCE.makeOptional(), Modifier.INOUT)
                                    .addStatement("result = fromObjectiveC(source)")
                                    .addStatement("return true")
                                    .returns(BOOL)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("_unconditionallyBridgeFromObjectiveC")
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference().makeOptional())
                                    .addStatement("return fromObjectiveC(source)")
                                    .returns(SelfTypeName.INSTANCE)
                                    .build()
                            )

                            addFunction(
                                FunctionSpec.builder("fromObjectiveC")
                                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                    .addParameter("_", "source", declarationReference.swiftReference().makeOptional())
                                    .addCode(
                                        CodeBlock.builder()
                                            .beginControlFlow("switch", "source")
                                            .add(
                                                declaredCases.map {
                                                    CodeBlock.of("case .%N?: return .%N", it.swiftReference(), it.swiftReference())
                                                }.joinToCode("\n", suffix = "\n")
                                            )
                                            .addStatement("default: fatalError(\"Couldn't map value of \\(String(describing: source)) to ${declarationReference.typeName}\")")
                                            .endControlFlow("switch")
                                            .build()
                                    )
                                    .returns(SelfTypeName.INSTANCE)
                                    .build()
                            )
                        }
                        .build()
                )
            }
        }
    }

    private fun shouldGenerateExhaustiveEnums(declaration: IrClass): Boolean {
        return declaration.isEnumClass
    }
}

package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.*
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.declarations.IrClass

internal class SealedFunctionGeneratorDelegate(
    override val configuration: SwiftGenConfiguration.SealedInteropDefaults,
    override val swiftPackModuleBuilder: SwiftPackModuleBuilder,
) : SealedGeneratorExtensionContainer {

    fun generate(declaration: IrClass, enumType: TypeName, fileBuilder: FileSpec.Builder) {
        fileBuilder.addFunction(
            FunctionSpec.builder(declaration.exhaustivelyFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(declaration.typeVariablesNames)
                .addParameter("_", "self", declaration.swiftNameWithTypeParameters)
                .returns(enumType)
                .addExhaustivelyFunctionBody(declaration, enumType)
                .build()
        )
    }

    private val IrClass.exhaustivelyFunctionName: String
        get() = this.findAnnotation<SealedInterop.FunctionName>()?.functionName ?: configuration.functionName

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: IrClass,
        enumType: TypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .addExhaustivelyCaseBranches(declaration, enumType)
            .addExhaustivelyFunctionEnd(declaration, enumType)
            .build()
    )

    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        declaration: IrClass,
        enumType: TypeName,
    ): CodeBlock.Builder {
        declaration.visibleSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val subclassName =
                    subclassSymbol.owner.swiftNameWithTypeParametersForSealedCase(declaration).canonicalName

                val condition = "let self = self as? $subclassName"

                if (index == 0) {
                    beginControlFlow("if", condition)
                } else {
                    nextControlFlow("else if", condition)
                }

                add("return ${enumType.canonicalName}.${subclassSymbol.enumCaseName}(self)\n")
            }

        return this
    }

    private fun CodeBlock.Builder.addExhaustivelyFunctionEnd(
        declaration: IrClass,
        enumType: TypeName,
    ): CodeBlock.Builder {
        if (declaration.hasAnyVisibleSealedSubclasses) {
            addExhaustivelyElseBranch(declaration, enumType)
        } else {
            addReturnElse(declaration, enumType)
        }

        return this
    }

    private val IrClass.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.sealedSubclasses.any { it.isVisibleSealedSubclass }

    private fun CodeBlock.Builder.addExhaustivelyElseBranch(declaration: IrClass, enumType: TypeName) {
        nextControlFlow("else")

        if (declaration.hasElseCase) {
            addReturnElse(declaration, enumType)
        } else {
            add(
                "fatalError(" +
                        "\"Unknown subtype. " +
                        "This error should not happen under normal circumstances " +
                        "since ${declaration.swiftName.canonicalName} is sealed." +
                        "\")\n"
            )
        }

        endControlFlow("else")
    }

    private fun CodeBlock.Builder.addReturnElse(declaration: IrClass, enumType: TypeName) {
        add("return ${enumType.canonicalName}.${declaration.elseCaseName}\n")
    }
}
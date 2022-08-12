package co.touchlab.swiftgen.plugin.internal.generator

import co.touchlab.swiftgen.api.SwiftSealed
import co.touchlab.swiftgen.api.SwiftSealedCase
import co.touchlab.swiftgen.plugin.internal.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.findAnnotation
import co.touchlab.swiftgen.plugin.internal.util.kotlinName
import co.touchlab.swiftgen.plugin.internal.util.swiftName
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

internal class SealedInteropGenerator(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
) : BaseGenerator<IrClass>(fileBuilderFactory, namespaceProvider) {

    // TODO Verify annotation usage on correct objects
    // TODO Verify that you cannot apply conflicting annotation
    // TODO Add configuration for Else
    // TODO Handle case when everything is hidden
    // TODO Add Global module configuration to Gradle

    override fun generate(declaration: IrClass) {
        if (!shouldGenerateSealedInterop(declaration)) {
            return
        }

        generateCode(declaration) {
            val classNamespace = addNamespace(swiftGenNamespace, declaration.kotlinName)

            val enumType = addSealedEnum(declaration, classNamespace)

            addExhaustivelyFunction(declaration, enumType)
        }
    }

    private fun shouldGenerateSealedInterop(declaration: IrClass): Boolean {
        val isSealed = declaration.sealedSubclasses.isNotEmpty()
        val isEnabled = declaration.findAnnotation<SwiftSealed.Disabled>() == null

        return isSealed && isEnabled
    }

    private fun FileSpec.Builder.addSealedEnum(
        declaration: IrClass,
        classNamespace: DeclaredTypeName,
    ): DeclaredTypeName {
        val enumName = "Enum"

        addExtension(
            ExtensionSpec.builder(classNamespace)
                .addModifiers(Modifier.PUBLIC)
                .addType(
                    TypeSpec.enumBuilder(enumName)
                        .addAttribute("frozen")
                        .addSealedEnumCases(declaration)
                        .build()
                )
                .build()
        )

        return classNamespace.nestedType(enumName)
    }

    private fun TypeSpec.Builder.addSealedEnumCases(declaration: IrClass): TypeSpec.Builder {
        declaration.sealedSubclasses
            .filterNot { it.isHiddenSealedSubclass }
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    sealedSubclass.owner.swiftName,
                )
            }

        if (declaration.hasAnyHiddenSealedSubclasses) {
            addEnumCase(declaration.elseCaseName)
        }

        return this
    }

    private fun FileSpec.Builder.addExhaustivelyFunction(declaration: IrClass, enumType: DeclaredTypeName) {
        addFunction(
            FunctionSpec.builder(getExhaustivelyFunctionName(declaration))
                .addModifiers(Modifier.PUBLIC)
                .addParameter("_", "self", declaration.swiftName)
                .returns(enumType)
                .addExhaustivelyFunctionBody(declaration, enumType)
                .build()
        )
    }

    private fun getExhaustivelyFunctionName(declaration: IrClass): String =
        declaration.findAnnotation<SwiftSealed.FunctionName>()?.functionName ?: "exhaustively"

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: IrClass,
        enumType: DeclaredTypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .apply {
                val sealedSubclasses = declaration.sealedSubclasses

                addExhaustivelyCaseBranches(sealedSubclasses, enumType)
                addExhaustivelyElseBranch(declaration, enumType)
            }
            .build()
    )

    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        sealedSubclasses: List<IrClassSymbol>,
        enumType: DeclaredTypeName,
    ) {
        sealedSubclasses
            .filterNot { it.isHiddenSealedSubclass }
            .forEachIndexed { index, sealedSubclassSymbol ->
                val condition = "let v = self as? ${sealedSubclassSymbol.owner.swiftName.canonicalName}"

                if (index == 0) {
                    beginControlFlow("if", condition)
                } else {
                    nextControlFlow("else if", condition)
                }

                add("return ${enumType.canonicalName}.${sealedSubclassSymbol.enumCaseName}(v)\n")
            }
    }

    private fun CodeBlock.Builder.addExhaustivelyElseBranch(
        declaration: IrClass,
        enumType: DeclaredTypeName,
    ) {
        nextControlFlow("else")

        if (declaration.hasAnyHiddenSealedSubclasses) {
            add("return ${enumType.canonicalName}.${declaration.elseCaseName}\n")
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

    private val IrClass.elseCaseName: String
        get() = "Else"

    private val IrClassSymbol.enumCaseName: String
        get() {
            val annotation = this.owner.findAnnotation<SwiftSealedCase.Name>()

            return annotation?.name ?: this.owner.name.identifier
        }

    private val IrClass.hasAnyHiddenSealedSubclasses: Boolean
        get() = this.sealedSubclasses.any { it.isHiddenSealedSubclass }

    private val IrClassSymbol.isHiddenSealedSubclass: Boolean
        get() {
            return this.owner.findAnnotation<SwiftSealedCase.Hidden>() != null
        }
}
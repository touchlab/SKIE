package co.touchlab.swiftgen.plugin.internal.generator

import co.touchlab.swiftgen.api.SealedInterop
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.*
import io.outfoxx.swiftpoet.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

internal class SealedInteropGenerator(
    fileBuilderFactory: FileBuilderFactory,
    namespaceProvider: NamespaceProvider,
    private val configuration: SwiftGenConfiguration.SealedInteropDefaults,
    private val reporter: Reporter,
) : BaseGenerator<IrClass>(fileBuilderFactory, namespaceProvider) {

    // TODO Verify annotation usage on correct objects
    // TODO Verify that you cannot apply conflicting annotation

    override fun generate(declaration: IrClass) {
        if (!shouldGenerateSealedInterop(declaration)) {
            return
        }

        generateCode(declaration) {
            val classNamespace = addNamespace(swiftGenNamespace, declaration.kotlinName)

            if (verifyUniqueCaseNames(declaration)) {
                val enumType = addSealedEnum(declaration, classNamespace)

                addExhaustivelyFunction(declaration, enumType)
            }
        }
    }

    private fun shouldGenerateSealedInterop(declaration: IrClass): Boolean {
        val isSealed = declaration.modality == Modality.SEALED

        val isVisible = declaration.visibility.isPublicAPI

        val isEnabled = if (configuration.enabled) {
            !declaration.hasAnnotation<SealedInterop.Disabled>()
        } else {
            declaration.hasAnnotation<SealedInterop.Enabled>()
        }

        return isSealed && isVisible && isEnabled
    }

    private fun verifyUniqueCaseNames(declaration: IrClass): Boolean {
        val conflictingDeclarations = declaration.sealedSubclasses
            .filter { it.isVisibleSealedSubclass }
            .groupBy { it.enumCaseName }
            .filter { it.value.size > 1 }

        conflictingDeclarations
            .forEach { (name, cases) ->
                cases.forEach { case ->
                    val message = "SwiftGen cannot generate sealed interop for this declaration. " +
                            "There are multiple sealed class/interface children with the same name `$name` for the enum case. " +
                            "Consider resolving this conflict using annotation `${SealedInterop.Case.Name::class.qualifiedName}`."

                    reporter.report(
                        severity = CompilerMessageSeverity.ERROR,
                        message = message,
                        declaration = case.owner,
                    )
                }
            }

        return conflictingDeclarations.isEmpty()
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
            .filter { it.isVisibleSealedSubclass }
            .forEach { sealedSubclass ->
                addEnumCase(
                    sealedSubclass.enumCaseName,
                    sealedSubclass.owner.swiftName,
                )
            }

        if (declaration.needsElseCase) {
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
        declaration.findAnnotation<SealedInterop.FunctionName>()?.functionName ?: configuration.functionName

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: IrClass,
        enumType: DeclaredTypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .apply {
                val sealedSubclasses = declaration.sealedSubclasses

                addExhaustivelyCaseBranches(sealedSubclasses, enumType)
                addExhaustivelyFunctionEnd(declaration, enumType)
            }
            .build()
    )

    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        sealedSubclasses: List<IrClassSymbol>,
        enumType: DeclaredTypeName,
    ) {
        sealedSubclasses
            .filter { it.isVisibleSealedSubclass }
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

    private fun CodeBlock.Builder.addExhaustivelyFunctionEnd(declaration: IrClass, enumType: DeclaredTypeName) {
        if (declaration.hasAnyVisibleSealedSubclasses) {
            addExhaustivelyElseBranch(declaration, enumType)
        } else {
            addReturnElse(declaration, enumType)
        }
    }

    private fun CodeBlock.Builder.addExhaustivelyElseBranch(declaration: IrClass, enumType: DeclaredTypeName) {
        nextControlFlow("else")

        if (declaration.needsElseCase) {
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

    private fun CodeBlock.Builder.addReturnElse(declaration: IrClass, enumType: DeclaredTypeName) {
        add("return ${enumType.canonicalName}.${declaration.elseCaseName}\n")
    }

    private val IrClass.elseCaseName: String
        get() = this.findAnnotation<SealedInterop.ElseName>()?.elseName ?: configuration.elseName

    private val IrClassSymbol.enumCaseName: String
        get() {
            val annotation = this.owner.findAnnotation<SealedInterop.Case.Name>()

            return annotation?.name ?: this.owner.name.identifier
        }

    private val IrClass.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.sealedSubclasses.any { it.isVisibleSealedSubclass }

    private val IrClass.needsElseCase: Boolean
        get() = this.sealedSubclasses.any { !it.isVisibleSealedSubclass } || this.sealedSubclasses.isEmpty()

    private val IrClassSymbol.isVisibleSealedSubclass: Boolean
        get() {
            val isVisible = owner.visibility.isPublicAPI

            val isEnabled = if (configuration.visibleCases) {
                !this.owner.hasAnnotation<SealedInterop.Case.Hidden>()
            } else {
                this.owner.hasAnnotation<SealedInterop.Case.Visible>()
            }

            return isVisible && isEnabled
        }
}
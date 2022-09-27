package co.touchlab.swiftgen.plugin.internal.sealed

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKeys
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedFunctionGeneratorDelegate(
    override val configuration: Configuration,
    override val swiftPackModuleBuilder: SwiftPackModuleBuilder,
) : SealedGeneratorExtensionContainer {

    fun generate(declaration: ClassDescriptor, enumType: TypeName, fileBuilder: FileSpec.Builder) {
        fileBuilder.addFunction(
            FunctionSpec.builder(declaration.enumConstructorFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(declaration.swiftTypeVariablesNames)
                .addParameter(
                    label = declaration.enumConstructorArgumentLabel,
                    name = declaration.enumConstructorParameterName,
                    type = declaration.swiftNameWithTypeParameters,
                )
                .returns(enumType)
                .addExhaustivelyFunctionBody(declaration, enumType)
                .build()
        )
    }

    private val ClassDescriptor.enumConstructorFunctionName: String
        get() = this.getConfiguration(ConfigurationKeys.SealedInterop.Function.Name)

    private val ClassDescriptor.enumConstructorArgumentLabel: String
        get() = this.getConfiguration(ConfigurationKeys.SealedInterop.Function.ArgumentLabel)

    private val ClassDescriptor.enumConstructorParameterName: String
        get() = this.getConfiguration(ConfigurationKeys.SealedInterop.Function.ParameterName)

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: ClassDescriptor,
        enumType: TypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .addExhaustivelyCaseBranches(declaration, enumType)
            .addExhaustivelyFunctionEnd(declaration, enumType)
            .build()
    )

    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        declaration: ClassDescriptor,
        enumType: TypeName,
    ): CodeBlock.Builder {
        declaration.visibleSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val parameterName = declaration.enumConstructorParameterName
                val subclassName = subclassSymbol.swiftNameWithTypeParametersForSealedCase(declaration).canonicalName

                val condition = "let $parameterName = $parameterName as? $subclassName"

                if (index == 0) {
                    beginControlFlow("if", condition)
                } else {
                    nextControlFlow("else if", condition)
                }

                add("return ${enumType.canonicalName}.${subclassSymbol.enumCaseName}($parameterName)\n")
            }

        return this
    }

    private fun CodeBlock.Builder.addExhaustivelyFunctionEnd(
        declaration: ClassDescriptor,
        enumType: TypeName,
    ): CodeBlock.Builder {
        if (declaration.hasAnyVisibleSealedSubclasses) {
            addExhaustivelyElseBranch(declaration, enumType)
        } else {
            addReturnElse(declaration, enumType)
        }

        return this
    }

    private val ClassDescriptor.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.sealedSubclasses.any { it.isVisibleSealedSubclass }

    private fun CodeBlock.Builder.addExhaustivelyElseBranch(declaration: ClassDescriptor, enumType: TypeName) {
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

    private fun CodeBlock.Builder.addReturnElse(declaration: ClassDescriptor, enumType: TypeName) {
        add("return ${enumType.canonicalName}.${declaration.elseCaseName}\n")
    }
}
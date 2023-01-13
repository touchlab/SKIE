package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.module.SwiftPoetScope
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class SealedFunctionGeneratorDelegate(
    override val descriptorProvider: DescriptorProvider,
    override val configuration: Configuration,
) : SealedGeneratorExtensionContainer, SwiftPoetExtensionContainer {

    context(SwiftPoetScope)
    fun generate(declaration: ClassDescriptor, enumType: TypeName, fileBuilder: FileSpec.Builder) {
        val enumGenericTypeParameter = declaration.enumGenericTypeParameter

        fileBuilder.addFunction(
            FunctionSpec.builder(declaration.enumConstructorFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(declaration.swiftTypeVariablesNames + enumGenericTypeParameter)
                .addParameter(
                    label = declaration.enumConstructorArgumentLabel,
                    name = declaration.enumConstructorParameterName,
                    type = enumGenericTypeParameter,
                )
                .returns(enumType)
                .addExhaustivelyFunctionBody(declaration, enumType)
                .build()
        )
    }

    private val ClassDescriptor.enumConstructorFunctionName: String
        get() = this.getConfiguration(SealedInterop.Function.Name)

    private val ClassDescriptor.enumConstructorArgumentLabel: String
        get() = this.getConfiguration(SealedInterop.Function.ArgumentLabel)

    private val ClassDescriptor.enumConstructorParameterName: String
        get() = this.getConfiguration(SealedInterop.Function.ParameterName)

    context(SwiftPoetScope)
    private val ClassDescriptor.enumGenericTypeParameter: TypeVariableName
        get() {
            val otherTypeNames = this.swiftTypeVariablesNames.map { it.name }

            val typeName = "SEALED".collisionFreeIdentifier(otherTypeNames)

            return TypeVariableName.typeVariable(typeName).withBounds(TypeVariableName.bound(swiftNameWithTypeParameters(this)))
        }

    context(SwiftPoetScope)
    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        declaration: ClassDescriptor,
        enumType: TypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .addExhaustivelyCaseBranches(declaration, enumType)
            .addExhaustivelyFunctionEnd(declaration, enumType)
            .build()
    )

    context(SwiftPoetScope)
    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        declaration: ClassDescriptor,
        enumType: TypeName,
    ): CodeBlock.Builder {
        declaration.explicitSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val parameterName = declaration.enumConstructorParameterName
                val subclassName = with(subclassSymbol) { swiftNameWithTypeParametersForSealedCase(declaration).canonicalName }

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

    context(SwiftPoetScope)
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
        get() = this.sealedSubclasses.any { it.isExplicitSealedSubclass }

    context(SwiftPoetScope)
    private fun CodeBlock.Builder.addExhaustivelyElseBranch(declaration: ClassDescriptor, enumType: TypeName) {
        nextControlFlow("else")

        if (declaration.hasElseCase) {
            addReturnElse(declaration, enumType)
        } else {
            add(
                "fatalError(" +
                    "\"Unknown subtype. " +
                    "This error should not happen under normal circumstances " +
                    "since ${declaration.swiftModel.bridgedOrStableFqName} is sealed." +
                    "\")\n"
            )
        }

        endControlFlow("else")
    }

    private fun CodeBlock.Builder.addReturnElse(declaration: ClassDescriptor, enumType: TypeName) {
        add("return ${enumType.canonicalName}.${declaration.elseCaseName}\n")
    }
}

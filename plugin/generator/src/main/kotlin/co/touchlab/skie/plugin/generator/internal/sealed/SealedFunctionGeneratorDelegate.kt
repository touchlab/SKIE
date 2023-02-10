package co.touchlab.skie.plugin.generator.internal.sealed

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.gradle.SealedInterop
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.generator.internal.util.SwiftPoetExtensionContainer
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

internal class SealedFunctionGeneratorDelegate(
    override val configuration: Configuration,
) : SealedGeneratorExtensionContainer, SwiftPoetExtensionContainer {

    fun generate(swiftModel: KotlinClassSwiftModel, enumType: TypeName, fileBuilder: FileSpec.Builder) {
        val enumGenericTypeParameter = swiftModel.enumGenericTypeParameter

        fileBuilder.addFunction(
            FunctionSpec.builder(swiftModel.enumConstructorFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(swiftModel.swiftTypeVariablesNames + enumGenericTypeParameter)
                .addParameter(
                    label = swiftModel.enumConstructorArgumentLabel,
                    name = swiftModel.enumConstructorParameterName,
                    type = enumGenericTypeParameter,
                )
                .returns(enumType)
                .addExhaustivelyFunctionBody(swiftModel, enumType)
                .build()
        )
    }

    private val KotlinClassSwiftModel.enumConstructorFunctionName: String
        get() = this.getConfiguration(SealedInterop.Function.Name)

    private val KotlinClassSwiftModel.enumConstructorArgumentLabel: String
        get() = this.getConfiguration(SealedInterop.Function.ArgumentLabel)

    private val KotlinClassSwiftModel.enumConstructorParameterName: String
        get() = this.getConfiguration(SealedInterop.Function.ParameterName)

    private val KotlinClassSwiftModel.enumGenericTypeParameter: TypeVariableName
        get() {
            val otherTypeNames = this.swiftTypeVariablesNames.map { it.name }

            val typeName = "SEALED".collisionFreeIdentifier(otherTypeNames)

            return TypeVariableName.typeVariable(typeName).withBounds(TypeVariableName.bound(this.swiftNameWithTypeParameters))
        }

    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        swiftModel: KotlinClassSwiftModel,
        enumType: TypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .addExhaustivelyCaseBranches(swiftModel, enumType)
            .addExhaustivelyFunctionEnd(swiftModel, enumType)
            .build()
    )

    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        swiftModel: KotlinClassSwiftModel,
        enumType: TypeName,
    ): CodeBlock.Builder {
        val preferredNamesCollide = swiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide

        swiftModel.visibleSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val parameterName = swiftModel.enumConstructorParameterName
                val subclassName = with(subclassSymbol) { swiftNameWithTypeParametersForSealedCase(swiftModel).canonicalName }

                val condition = "let $parameterName = $parameterName as? $subclassName"

                if (index == 0) {
                    beginControlFlow("if", condition)
                } else {
                    nextControlFlow("else if", condition)
                }

                add("return ${enumType.canonicalName}.${subclassSymbol.enumCaseName(preferredNamesCollide)}($parameterName)\n")
            }

        return this
    }

    private fun CodeBlock.Builder.addExhaustivelyFunctionEnd(
        swiftModel: KotlinClassSwiftModel,
        enumType: TypeName,
    ): CodeBlock.Builder {
        if (swiftModel.hasAnyVisibleSealedSubclasses) {
            addExhaustivelyElseBranch(swiftModel, enumType)
        } else {
            addReturnElse(swiftModel, enumType)
        }

        return this
    }

    private val KotlinClassSwiftModel.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.visibleSealedSubclasses.isNotEmpty()

    private fun CodeBlock.Builder.addExhaustivelyElseBranch(swiftModel: KotlinClassSwiftModel, enumType: TypeName) {
        nextControlFlow("else")

        if (swiftModel.hasElseCase) {
            addReturnElse(swiftModel, enumType)
        } else {
            add(
                "fatalError(" +
                    "\"Unknown subtype. " +
                    "This error should not happen under normal circumstances " +
                    "since ${swiftModel.bridgedOrStableFqName} is sealed." +
                    "\")\n"
            )
        }

        endControlFlow("else")
    }

    private fun CodeBlock.Builder.addReturnElse(swiftModel: KotlinClassSwiftModel, enumType: TypeName) {
        add("return ${enumType.canonicalName}.${swiftModel.elseCaseName}\n")
    }
}

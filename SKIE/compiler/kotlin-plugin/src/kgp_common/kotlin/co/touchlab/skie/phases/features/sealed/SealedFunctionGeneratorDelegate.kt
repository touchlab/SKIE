package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.TypeName

class SealedFunctionGeneratorDelegate(
    override val context: SirPhase.Context,
) : SealedGeneratorExtensionContainer {

    context(SwiftModelScope)
    fun generate(swiftModel: KotlinClassSwiftModel, enum: SirClass) {
        SirFunction(
            identifier = swiftModel.enumConstructorFunctionName,
            parent = sirProvider.getFile(swiftModel),
            returnType = enum.toTypeFromEnclosingTypeParameters(enum.typeParameters),
        ).apply {
            copyTypeParametersFrom(enum)

            val sealedTypeParameter = SirTypeParameter(
                name = "__Sealed",
                bounds = listOf(
                    swiftModel.kotlinSirClass.toTypeFromEnclosingTypeParameters(typeParameters),
                ),
            )

            SirValueParameter(
                label = swiftModel.enumConstructorArgumentLabel,
                name = swiftModel.enumConstructorParameterName,
                type = sealedTypeParameter.toTypeParameterUsage(),
            )

            addFunctionBody(swiftModel, enum)
        }
    }

    private val KotlinClassSwiftModel.enumConstructorFunctionName: String
        get() = configurationProvider.getConfiguration(this, SealedInterop.Function.Name)

    private val KotlinClassSwiftModel.enumConstructorArgumentLabel: String
        get() = configurationProvider.getConfiguration(this, SealedInterop.Function.ArgumentLabel)

    private val KotlinClassSwiftModel.enumConstructorParameterName: String
        get() = configurationProvider.getConfiguration(this, SealedInterop.Function.ParameterName)

    context(SwiftModelScope)
    private fun SirFunction.addFunctionBody(
        swiftModel: KotlinClassSwiftModel,
        enum: SirClass,
    ) {
        swiftPoetBuilderModifications.add {
            val enumType = enum.toTypeFromEnclosingTypeParameters(enum.typeParameters).toSwiftPoetTypeName()

            addCode(
                CodeBlock.builder()
                    .addCaseBranches(swiftModel, enum, enumType)
                    .addFunctionEnd(swiftModel, enumType)
                    .build(),
            )
        }
    }

    context(SwiftModelScope)
    private fun CodeBlock.Builder.addCaseBranches(
        swiftModel: KotlinClassSwiftModel,
        enum: SirClass,
        enumType: TypeName,
    ): CodeBlock.Builder {
        val preferredNamesCollide = swiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide

        swiftModel.visibleSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val parameterName = swiftModel.enumConstructorParameterName
                val subclassName = subclassSymbol.primarySirClass.getSealedSubclassType(enum, this@SwiftModelScope).toSwiftPoetTypeName()

                val condition = "let %N = %N as? %T"

                if (index == 0) {
                    beginControlFlow("if", condition, parameterName, parameterName, subclassName)
                } else {
                    nextControlFlow("else if", condition, parameterName, parameterName, subclassName)
                }

                add("return %T.%N(%N)\n", enumType, subclassSymbol.enumCaseName(preferredNamesCollide), parameterName)
            }

        return this
    }

    private fun CodeBlock.Builder.addFunctionEnd(
        swiftModel: KotlinClassSwiftModel,
        enumType: TypeName,
    ): CodeBlock.Builder {
        if (swiftModel.hasAnyVisibleSealedSubclasses) {
            addElseBranch(swiftModel, enumType)
        } else {
            addReturnElse(swiftModel, enumType)
        }

        return this
    }

    private val KotlinClassSwiftModel.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.visibleSealedSubclasses.isNotEmpty()

    private fun CodeBlock.Builder.addElseBranch(swiftModel: KotlinClassSwiftModel, enumType: TypeName) {
        nextControlFlow("else")

        if (swiftModel.hasElseCase) {
            addReturnElse(swiftModel, enumType)
        } else {
            add(
                "fatalError(" +
                        "\"Unknown subtype. " +
                        "This error should not happen under normal circumstances " +
                        "since ${swiftModel.primarySirClass} is sealed." +
                        "\")\n",
            )
        }

        endControlFlow("else")
    }

    private fun CodeBlock.Builder.addReturnElse(swiftModel: KotlinClassSwiftModel, enumType: TypeName) {
        add("return %T.%N\n", enumType, swiftModel.elseCaseName)
    }
}

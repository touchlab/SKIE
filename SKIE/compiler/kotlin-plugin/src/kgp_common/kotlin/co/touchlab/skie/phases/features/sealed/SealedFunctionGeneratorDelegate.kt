package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.toSwiftPoetVariables
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName

internal class SealedFunctionGeneratorDelegate(
    override val skieContext: SkieContext,
) : SealedGeneratorExtensionContainer {

    context(SwiftModelScope)
    fun generate(swiftModel: KotlinClassSwiftModel, enum: SirClass) {
        val enumType = enum.toTypeFromEnclosingTypeParameters(enum.typeParameters).toSwiftPoetUsage()

        val kotlinType = swiftModel.kotlinSirClass.toTypeFromEnclosingTypeParameters(enum.typeParameters).toSwiftPoetUsage()

        val enumSelfTypeParameter = TypeVariableName.typeVariable("__Sealed").withBounds(TypeVariableName.bound(kotlinType))

        sirProvider.getFile(swiftModel).swiftPoetBuilderModifications.add {
            addFunction(
                FunctionSpec.builder(swiftModel.enumConstructorFunctionName)
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariables(enum.typeParameters.toSwiftPoetVariables() + enumSelfTypeParameter)
                    .addParameter(
                        label = swiftModel.enumConstructorArgumentLabel,
                        name = swiftModel.enumConstructorParameterName,
                        type = enumSelfTypeParameter,
                    )
                    .returns(enumType)
                    .addExhaustivelyFunctionBody(swiftModel, enum, enumType)
                    .build(),
            )
        }
    }

    private val KotlinClassSwiftModel.enumConstructorFunctionName: String
        get() = this.getConfiguration(SealedInterop.Function.Name)

    private val KotlinClassSwiftModel.enumConstructorArgumentLabel: String
        get() = this.getConfiguration(SealedInterop.Function.ArgumentLabel)

    private val KotlinClassSwiftModel.enumConstructorParameterName: String
        get() = this.getConfiguration(SealedInterop.Function.ParameterName)

    context(SwiftModelScope)
    private fun FunctionSpec.Builder.addExhaustivelyFunctionBody(
        swiftModel: KotlinClassSwiftModel,
        enum: SirClass,
        enumType: TypeName,
    ): FunctionSpec.Builder = addCode(
        CodeBlock.builder()
            .addExhaustivelyCaseBranches(swiftModel, enum, enumType)
            .addExhaustivelyFunctionEnd(swiftModel, enumType)
            .build(),
    )

    context(SwiftModelScope)
    private fun CodeBlock.Builder.addExhaustivelyCaseBranches(
        swiftModel: KotlinClassSwiftModel,
        enum: SirClass,
        enumType: TypeName,
    ): CodeBlock.Builder {
        val preferredNamesCollide = swiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide

        swiftModel.visibleSealedSubclasses
            .forEachIndexed { index, subclassSymbol ->
                val parameterName = swiftModel.enumConstructorParameterName
                val subclassName = subclassSymbol.primarySirClass.getSealedSubclassType(enum, this@SwiftModelScope).toSwiftPoetUsage()

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

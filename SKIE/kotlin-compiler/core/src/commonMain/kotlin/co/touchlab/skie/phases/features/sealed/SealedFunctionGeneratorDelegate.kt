package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.util.MustBeExecutedAfterBridgingConfiguration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.call
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toConformanceBound
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters
import co.touchlab.skie.sir.element.toTypeParameterUsage
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.toNullable
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.TypeName

@MustBeExecutedAfterBridgingConfiguration
class SealedFunctionGeneratorDelegate(override val context: SirPhase.Context) : SealedGeneratorExtensionContainer {

    context(SirPhase.Context)
    fun generate(kirClass: KirClass, enum: SirClass) {
        val requiredFunction = generateRequiredOverload(kirClass, enum)
        generateOptionalOverload(kirClass, enum, requiredFunction)
    }

    private fun generateRequiredOverload(kirClass: KirClass, enum: SirClass): SirSimpleFunction =
        createFunctionDeclaration(kirClass, enum).apply {
            addRequiredFunctionBody(kirClass, enum)
        }

    private fun generateOptionalOverload(kirClass: KirClass, enum: SirClass, requiredFunction: SirSimpleFunction) {
        createFunctionDeclaration(
            kirClass = kirClass,
            enum = enum,
            valueParameterType = { it.toNullable() },
            returnTypeModifier = { it.toNullable() },
        ).apply {
            attributes.add("_disfavoredOverload")

            addOptionalFunctionBody(requiredFunction)
        }
    }

    private fun createFunctionDeclaration(
        kirClass: KirClass,
        enum: SirClass,
        valueParameterType: (SirType) -> SirType = { it },
        returnTypeModifier: (SirType) -> SirType = { it },
    ): SirSimpleFunction = SirSimpleFunction(
        identifier = kirClass.enumConstructorFunctionName,
        parent = context.namespaceProvider.getNamespaceFile(kirClass),
        returnType = enum.toTypeFromEnclosingTypeParameters(enum.typeParameters).let(returnTypeModifier),
    ).apply {
        copyTypeParametersFrom(enum)

        val sealedTypeParameter = SirTypeParameter(
            name = "__Sealed",
            bounds = listOf(
                kirClass.originalSirClass.toTypeFromEnclosingTypeParameters(typeParameters).toConformanceBound(),
            ),
        )

        SirValueParameter(
            label = kirClass.enumConstructorArgumentLabel,
            name = kirClass.enumConstructorParameterName,
            type = sealedTypeParameter.toTypeParameterUsage().let(valueParameterType),
        )
    }

    private val KirClass.enumConstructorFunctionName: String
        get() = this.configuration[SealedInterop.Function.Name]

    private val KirClass.enumConstructorArgumentLabel: String
        get() = this.configuration[SealedInterop.Function.ArgumentLabel]

    private val KirClass.enumConstructorParameterName: String
        get() = this.configuration[SealedInterop.Function.ParameterName]

    private fun SirSimpleFunction.addRequiredFunctionBody(kirClass: KirClass, enum: SirClass) {
        bodyBuilder.add {
            val enumType = enum.toTypeFromEnclosingTypeParameters(enum.typeParameters).evaluate().swiftPoetTypeName

            addCode(
                CodeBlock.builder()
                    .addCaseBranches(kirClass, enum, enumType)
                    .addFunctionEnd(kirClass, enumType)
                    .build(),
            )
        }
    }

    private fun CodeBlock.Builder.addCaseBranches(kirClass: KirClass, enum: SirClass, enumType: TypeName): CodeBlock.Builder {
        val preferredNamesCollide = kirClass.enumCaseNamesBasedOnKotlinIdentifiersCollide
        val parameterName = kirClass.enumConstructorParameterName

        /*
            When the `onEnum(of:)` gets specialized,
            the Swift optimizer sees `sealed` as a specific type.
            When that specific type isn't statically castable to one of the classes in `kirClass.visibleSealedSubclasses`,
            Swift optimizer removes that code as it's unreachable from its point of view.
            In certain cases that can result in reaching the `fatalError` in Release mode.
            Adding a variable with explicit type `Any` and then casting that variable gets past the optimizer.
         */
        val sourceVariableName = if (kirClass.typeParameters.isNotEmpty()) {
            val erasedVariableName = "erased"
            addStatement("let %N: Any = %N", erasedVariableName, parameterName)
            erasedVariableName
        } else {
            parameterName
        }

        kirClass.visibleSealedSubclasses
            .forEachIndexed { index, subclass ->
                val subclassName = subclass.primarySirClass.getSealedSubclassType(enum).evaluate().swiftPoetTypeName

                val condition = "let %N = %N as? %T"

                if (index == 0) {
                    beginControlFlow("if", condition, sourceVariableName, sourceVariableName, subclassName)
                } else {
                    nextControlFlow("else if", condition, sourceVariableName, sourceVariableName, subclassName)
                }

                add("return %T.%N(%N)\n", enumType, subclass.enumCaseName(preferredNamesCollide), sourceVariableName)
            }

        return this
    }

    private fun CodeBlock.Builder.addFunctionEnd(kirClass: KirClass, enumType: TypeName): CodeBlock.Builder {
        if (kirClass.hasAnyVisibleSealedSubclasses) {
            addElseBranch(kirClass, enumType)
        } else {
            addReturnElse(kirClass, enumType)
        }

        return this
    }

    private val KirClass.hasAnyVisibleSealedSubclasses: Boolean
        get() = this.visibleSealedSubclasses.isNotEmpty()

    private fun CodeBlock.Builder.addElseBranch(kirClass: KirClass, enumType: TypeName) {
        nextControlFlow("else")

        if (kirClass.hasElseCase) {
            addReturnElse(kirClass, enumType)
        } else {
            add(
                "fatalError(" +
                    "\"Unknown subtype \\(%N). " +
                    "This error should not happen under normal circumstances " +
                    "since ${kirClass.originalSirClass} is sealed." +
                    "\")\n",
                kirClass.enumConstructorParameterName,
            )
        }

        endControlFlow("else")
    }

    private fun CodeBlock.Builder.addReturnElse(kirClass: KirClass, enumType: TypeName) {
        add("return %T.%N\n", enumType, kirClass.elseCaseName)
    }

    private fun SirSimpleFunction.addOptionalFunctionBody(requiredFunction: SirSimpleFunction) {
        bodyBuilder.add {
            val valueParameter = valueParameters.first()

            addCode(
                CodeBlock.builder()
                    .beginControlFlow("if", "let ${valueParameter.name.escapeSwiftIdentifier()}")
                    .add("return %L as %T", requiredFunction.call(valueParameter), requiredFunction.returnType.evaluate().swiftPoetTypeName)
                    .nextControlFlow("else")
                    .add("return nil")
                    .endControlFlow("else")
                    .build(),
            )
        }
    }
}

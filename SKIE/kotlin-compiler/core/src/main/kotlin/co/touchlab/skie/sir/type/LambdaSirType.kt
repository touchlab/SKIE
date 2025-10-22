package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.minimumVisibility
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec

data class LambdaSirType(
    val valueParameterTypes: List<SirType>,
    val returnType: SirType,
    val isEscaping: Boolean,
    val isAsync: Boolean = false,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isReference: Boolean = false

    override fun evaluate(): EvaluatedSirType {
        val evaluatedValueParameterTypes = lazy { valueParameterTypes.map { it.evaluate() } }
        val evaluatedReturnType = lazy { returnType.evaluate() }

        return EvaluatedSirType.Lazy(
            typeProvider = lazy {
                copy(valueParameterTypes = evaluatedValueParameterTypes.value.map { it.type }, returnType = evaluatedReturnType.value.type)
            },
            canonicalNameProvider = lazy {
                "((${evaluatedValueParameterTypes.value.joinToString { it.canonicalName }}) -> ${evaluatedReturnType.value.canonicalName})"
            },
            swiftPoetTypeNameProvider = lazy {
                FunctionTypeName.get(
                    parameters = evaluatedValueParameterTypes.value.map { ParameterSpec.unnamed(it.swiftPoetTypeName) },
                    returnType = evaluatedReturnType.value.swiftPoetTypeName,
                    attributes = if (isEscaping) {
                        listOf(AttributeSpec.ESCAPING)
                    } else {
                        emptyList()
                    },
                    async = isAsync,
                )
            },
            lowestVisibility = lazy {
                (evaluatedValueParameterTypes.value.map { it.visibilityConstraint } + evaluatedReturnType.value.visibilityConstraint).minimumVisibility()
            },
            referencedTypeDeclarationsProvider = lazy {
                (evaluatedValueParameterTypes.value.flatMap { it.referencedTypeDeclarations } + evaluatedReturnType.value.referencedTypeDeclarations).toSet()
            },
        )
    }

    override fun inlineTypeAliases(): SirType =
        copy(
            valueParameterTypes = valueParameterTypes.map { it.inlineTypeAliases() },
            returnType = returnType.inlineTypeAliases(),
        )

    // TODO: Probably use `copy` here to make sure we don't have to manually specify all parameters
    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): LambdaSirType =
        LambdaSirType(
            returnType = returnType.substituteTypeParameters(substitutions),
            valueParameterTypes = valueParameterTypes.map { it.substituteTypeParameters(substitutions) },
            isEscaping = isEscaping,
            isAsync = isAsync,
        )

    // TODO: Probably use `copy` here to make sure we don't have to manually specify all parameters
    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): LambdaSirType =
        LambdaSirType(
            returnType = returnType.substituteTypeArguments(substitutions),
            valueParameterTypes = valueParameterTypes.map { it.substituteTypeArguments(substitutions) },
            isEscaping = isEscaping,
            isAsync = isAsync,
        )
}

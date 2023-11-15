package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeParameter
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec

data class LambdaSirType(
    val valueParameterTypes: List<SirType>,
    val returnType: SirType,
    val isEscaping: Boolean,
) : NonNullSirType() {

    override val isHashable: Boolean = false

    override val isReference: Boolean = false

    override fun evaluate(): EvaluatedSirType<LambdaSirType> {
        val evaluatedValueParameterTypes = valueParameterTypes.map { it.evaluate() }
        val evaluatedReturnType = returnType.evaluate()

        return EvaluatedSirType(
            type = copy(valueParameterTypes = evaluatedValueParameterTypes.map { it.type }, returnType = evaluatedReturnType.type),
            isValid = (evaluatedValueParameterTypes + evaluatedReturnType).all { it.isValid },
            canonicalName = "((${evaluatedValueParameterTypes.joinToString { it.canonicalName }}) -> ${evaluatedReturnType.canonicalName})",
            swiftPoetTypeName = FunctionTypeName.get(
                parameters = evaluatedValueParameterTypes.map { ParameterSpec.unnamed(it.swiftPoetTypeName) },
                returnType = evaluatedReturnType.swiftPoetTypeName,
                attributes = if (isEscaping) {
                    listOf(AttributeSpec.ESCAPING)
                } else {
                    emptyList()
                },
            ),
        )
    }

    override fun inlineTypeAliases(): SirType =
        copy(
            valueParameterTypes = valueParameterTypes.map { it.inlineTypeAliases() },
            returnType = returnType.inlineTypeAliases(),
        )

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): LambdaSirType =
        LambdaSirType(
            returnType = returnType.substituteTypeParameters(substitutions),
            valueParameterTypes = valueParameterTypes.map { it.substituteTypeParameters(substitutions) },
            isEscaping = isEscaping,
        )

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): LambdaSirType =
        LambdaSirType(
            returnType = returnType.substituteTypeArguments(substitutions),
            valueParameterTypes = valueParameterTypes.map { it.substituteTypeArguments(substitutions) },
            isEscaping = isEscaping,
        )
}

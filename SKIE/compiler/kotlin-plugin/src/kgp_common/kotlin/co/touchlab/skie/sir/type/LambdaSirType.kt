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
                )
            },
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

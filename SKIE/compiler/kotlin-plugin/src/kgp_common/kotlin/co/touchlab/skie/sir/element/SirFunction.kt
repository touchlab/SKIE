package co.touchlab.skie.sir.element

import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

sealed class SirFunction(
    override val attributes: MutableList<String>,
    override val modifiers: MutableList<Modifier>,
) : SirCallableDeclaration, SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    abstract var throws: Boolean

    abstract val valueParameters: MutableList<SirValueParameter>

    override val swiftPoetBuilderModifications = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    override val hasValidSignature: Boolean
        get() = valueParameters.all { it.type.evaluate().isValid }
}

fun SirFunction.copyValueParametersFrom(other: SirFunction) {
    copyValueParametersFrom(other.valueParameters)
}

fun SirFunction.copyValueParametersFrom(valueParameters: List<SirValueParameter>) {
    valueParameters.map {
        SirValueParameter(
            label = it.label,
            name = it.name,
            // TODO Substitute type parameter usage
            type = it.type,
            inout = it.inout,
        )
    }
}

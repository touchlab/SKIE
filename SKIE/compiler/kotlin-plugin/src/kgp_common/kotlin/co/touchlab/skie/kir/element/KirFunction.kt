package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.FunctionConfiguration
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.sir.element.SirFunction

sealed class KirFunction<S : SirFunction> : KirCallableDeclaration<S> {

    abstract val kotlinName: String

    abstract val objCSelector: String

    abstract val swiftName: String

    abstract val errorHandlingStrategy: OirFunction.ErrorHandlingStrategy

    abstract val valueParameters: MutableList<KirValueParameter>

    abstract val defaultArgumentsOverloads: List<KirFunction<S>>

    abstract override val configuration: FunctionConfiguration

    override fun toString(): String = "${this::class.simpleName}: $kotlinName ($owner)"
}

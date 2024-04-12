package co.touchlab.skie.sir.element

import co.touchlab.skie.phases.memberconflicts.Signature
import co.touchlab.skie.phases.memberconflicts.SirHierarchyCache
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

sealed class SirFunction(
    override val attributes: MutableList<String>,
    override val modifiers: MutableList<Modifier>,
) : SirCallableDeclaration, SirElementWithFunctionBodyBuilder {

    abstract var throws: Boolean

    abstract val valueParameters: MutableList<SirValueParameter>

    override val bodyBuilder = mutableListOf<FunctionSpec.Builder.() -> Unit>()

    override val reference: String
        get() = if (valueParameters.isEmpty()) {
            identifierForReference
        } else {
            "$identifierForReference(${valueParameters.joinToString("") { "${it.labelOrName}:" }})"
        }

    override val name: String
        get() = if (valueParameters.isEmpty()) {
            "$identifierAfterVisibilityChange()"
        } else {
            "$identifierAfterVisibilityChange(${valueParameters.joinToString("") { "${it.labelOrName}:" }})"
        }

    /**
     * Creates interpolated string for SwiftPoet that represents a function call to this function.
     */
    fun call(arguments: List<String>): String {
        require(valueParameters.size == arguments.size) {
            "Expected ${valueParameters.size} arguments, but got ${arguments.size} for $this"
        }

        val argumentsWithLabels = valueParameters.zip(arguments)
            .joinToString(", ") { (parameter, argument) ->
                if (parameter.label == "_") argument else CodeBlock.toString("%N: ", parameter.labelOrName) + argument
            }

        return "$identifierForReference($argumentsWithLabels)"
    }

    fun call(vararg arguments: String): String =
        call(arguments.toList())

    protected abstract val identifierForReference: String

    override fun toString(): String =
        Signature(this).toString()
}

fun SirFunction.call(arguments: List<SirValueParameter>): String =
    call(arguments.map { it.name.escapeSwiftIdentifier() })

fun SirFunction.call(vararg arguments: SirValueParameter): String =
    call(arguments.toList())

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

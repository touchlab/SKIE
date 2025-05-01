package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.OirType

sealed class OirFunction(final override val parent: OirCallableDeclarationParent) : OirCallableDeclaration {

    abstract val selector: String

    abstract val errorHandlingStrategy: ErrorHandlingStrategy

    abstract val returnType: OirType?

    val baseSelector: String
        get() = selector.substringBefore(':')

    val valueParameters: MutableList<OirValueParameter> = mutableListOf()

    init {
        @Suppress("LeakingThis")
        parent.callableDeclarations.add(this)
    }

    override fun toString(): String = "${this::class.simpleName}: $selector"

    enum class ErrorHandlingStrategy {
        Crashes,
        ReturnsBoolean,
        ReturnsZero,
        SetsErrorOut,
        ;

        val isThrowing: Boolean
            get() = this != Crashes
    }
}

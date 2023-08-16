package co.touchlab.skie.api.phases.util

sealed interface ExternalType {

    val module: String

    val name: String

    data class Class(
        override val module: String,
        override val name: String,
        val typeParameterCount: Int,
    ) : ExternalType

    data class Protocol(
        override val module: String,
        override val name: String,
    ) : ExternalType
}

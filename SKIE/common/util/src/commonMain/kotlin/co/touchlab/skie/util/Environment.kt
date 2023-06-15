package co.touchlab.skie.util

import kotlinx.serialization.Serializable

@Serializable
enum class Environment {

    Production, Dev, Tests, Unknown;

    fun canBeProduction(): Boolean =
        this == Production || this == Unknown
}

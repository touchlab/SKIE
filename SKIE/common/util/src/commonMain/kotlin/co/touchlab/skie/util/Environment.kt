package co.touchlab.skie.util

import kotlinx.serialization.Serializable

@Serializable
enum class Environment {

    Production, Dev, Tests, Unknown;

    fun canBeProduction(): Boolean =
        this == Production || this == Unknown

    companion object {

        val current: Environment =
            if (BuildConfig.SKIE_VERSION.contains("SNAPSHOT")) Dev else Production
    }
}

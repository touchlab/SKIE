package co.touchlab.skie.plugin.libraries.lockfile

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Lockfile(
    // Cannot be a map because we need to preserve ordering
    val libraries: List<Library>,
) {

    @Serializable
    data class Library(
        val index: Int,
        val coordinate: String,
        val dependencies: List<String>,
    )
}

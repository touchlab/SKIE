package co.touchlab.skie.plugin.libraries.library

data class Artifacts(
    val all: List<Artifact>,
    val exported: List<Artifact>,
) {

    companion object {

        val empty = Artifacts(emptyList(), emptyList())
    }
}

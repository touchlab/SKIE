package co.touchlab.skie.plugin.libraries

data class ExternalLibraryTest(
    val index: Int,
    val library: String,
    val exportedLibraries: List<String>,
) {
    val directoryName = "${index}-${library.replace(" ", "-").replace(":", "-")}"
}

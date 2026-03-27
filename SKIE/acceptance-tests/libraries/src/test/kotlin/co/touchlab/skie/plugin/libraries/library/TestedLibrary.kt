package co.touchlab.skie.plugin.libraries.library

data class TestedLibrary(
    val index: Int,
    val component: Component,
    val dependencies: List<Component>,
) {

    val fullName: String
        get() = "[$index]: $component"

    override fun toString(): String = component.coordinate
}

fun List<TestedLibrary>.mergeWith(other: List<TestedLibrary>): List<TestedLibrary> {
    val existingModules = this.map { it.component.module }.toSet()

    return (this + other.filter { it.component.module !in existingModules })
        .distinct()
        .withFixedIndices()
}

private fun List<TestedLibrary>.withFixedIndices(): List<TestedLibrary> =
    mapIndexed { index, library ->
        library.copy(index = index)
    }

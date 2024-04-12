package co.touchlab.skie.test.runner

import co.touchlab.skie.test.annotation.filter.OnlyFor
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.LinkMode

data class MatrixFilter(
    val targets: Set<KotlinTarget>,
    val configurations: Set<BuildConfiguration>,
    val linkModes: Set<LinkMode>,
    val kotlinVersions: Set<String>,
) {
    fun apply(onlyFor: OnlyFor): MatrixFilter = MatrixFilter(
        targets = targets.intersectOrChoose(onlyFor.targets.map { it.target }),
        configurations = configurations.intersectOrChoose(onlyFor.configurations.toList()),
        linkModes = linkModes.intersectOrChoose(onlyFor.linkModes.toList()),
        kotlinVersions = kotlinVersions.intersectOrChoose(onlyFor.kotlinVersions.toList()),
    )

    companion object {
        val empty = MatrixFilter(
            targets = emptySet(),
            configurations = emptySet(),
            linkModes = emptySet(),
            kotlinVersions = emptySet(),
        )

        private inline fun <reified T> Set<T>.intersectOrChoose(other: Collection<T>): Set<T> {
            return when {
                this.isEmpty() -> other.toSet()
                other.isEmpty() -> this
                else -> intersect(other.toSet())
            }
        }
    }
}

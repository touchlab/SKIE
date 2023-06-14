package co.touchlab.skie.gradle.version

class VersionHolder<VERSION: Comparable<VERSION>>(
    val version: VERSION,
    val identifier: String,
): Comparable<VersionHolder<VERSION>> {
    val targetName: String = identifier
    fun sourceSetName(compilation: SourceSetScope.Compilation): String = "$targetName${compilation.name}"

    override fun compareTo(other: VersionHolder<VERSION>): Int {
        return version.compareTo(other.version)
    }
}

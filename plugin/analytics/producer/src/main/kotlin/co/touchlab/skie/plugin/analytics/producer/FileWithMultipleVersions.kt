package co.touchlab.skie.plugin.analytics.producer

import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.name

data class FileWithMultipleVersions(val versions: List<Path>) {

    val newest: Path
        get() = versions.maxBy { getVersion(it) }

    fun deleteAll() {
        versions.forEach {
            it.deleteIfExists()
        }
    }

    companion object {

        fun nameWithoutVersion(path: Path): String =
            path.name.substringBeforeLast(".")

        fun addVersion(name: String, version: Int): String =
            "$name.$version"

        fun getVersion(path: Path): Int =
            path.name.substringAfterLast(".").toInt()
    }
}

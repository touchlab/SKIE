package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.producer.compressor.CompressionMethod
import co.touchlab.skie.util.Environment
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class AnalyticsArtifact(
    val buildId: String,
    val type: String,
    val skieVersion: String,
    val environment: Environment,
    val compressionMethod: CompressionMethod,
) {

    private val fileNameWithoutCompressionName = fileNameWithoutCompressionName(
        buildId = buildId,
        type = type,
        skieVersion = skieVersion,
        environment = environment.name,
    )

    val fileName = "$fileNameWithoutCompressionName.${compressionMethod.name}"

    fun path(analyticsDirectory: Path): Path =
        analyticsDirectory.resolve(fileName)

    fun deleteFrom(analyticsDirectory: Path) {
        analyticsDirectory.listDirectoryEntries()
            .filter { it.name.startsWith("$fileNameWithoutCompressionName.") }
            .forEach {
                it.deleteIfExists()
            }
    }

    companion object {

        fun fileNameWithoutCompressionName(
            buildId: String,
            type: String,
            skieVersion: String,
            environment: String,
        ): String = "$buildId.$type.${encodeVersion(skieVersion)}.$environment"

        fun fromFilesDeduplicated(files: List<Path>): List<AnalyticsArtifact> =
            files.groupBy { nameWithoutCompressionName(it) }
                .mapNotNull {
                    try {
                        parseArtifactWithMultipleVersions(it.value)
                    } catch (_: Throwable) {
                        null
                    }
                }

        private fun parseArtifactWithMultipleVersions(versions: List<Path>): AnalyticsArtifact {
            val versionsWithCompressionMethod = versions
                .map { it to CompressionMethod.valueOf(it.name.substringAfterLast(".")) }

            val bestVersion = versionsWithCompressionMethod.maxBy { it.second.priority }.first

            return parseArtifact(bestVersion)
        }

        private fun parseArtifact(file: Path): AnalyticsArtifact {
            val nameComponents = file.name.split(".")

            check(nameComponents.size == 5) { "File name ${file.name} has incorrect format." }

            return AnalyticsArtifact(
                buildId = nameComponents[0],
                type = nameComponents[1],
                skieVersion = decodeVersion(nameComponents[2]),
                environment = Environment.valueOf(nameComponents[3]),
                compressionMethod = CompressionMethod.valueOf(nameComponents[4]),
            )
        }

        private fun encodeVersion(version: String): String =
            version.replace(".", "_")

        private fun decodeVersion(version: String): String =
            version.replace("_", ".")

        private fun nameWithoutCompressionName(path: Path): String =
            path.name.substringBeforeLast(".")

        private val CompressionMethod.priority: Int
            get() = when (this) {
                CompressionMethod.Deflate -> 0
                CompressionMethod.Bzip2 -> 1
            }
    }
}

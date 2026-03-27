package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.deleteIfExists
import kotlin.math.pow

class TestDirectoryManager(
    testTmpDir: File,
) {

    private val filesDir: File = testTmpDir.resolve("files").also { it.mkdirs() }
    private val indicesDir: File = testTmpDir.resolve("indices").also { it.mkdirs() }

    fun createDirectories(libraries: List<TestedLibrary>) {
        Files.walkFileTree(
            indicesDir.toPath(),
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    file?.deleteIfExists()
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                    dir?.deleteIfExists()
                    return FileVisitResult.CONTINUE
                }
            },
        )

        val rootRange = Range(libraries.size)
        val linksToCreate = libraries.map {
            linkFileForIndex(it.index, rootRange) to directoryForLibrary(it)
        }

        linksToCreate.map { (link, target) ->
            Files.createSymbolicLink(link.toPath(), target.toPath())
        }
    }

    fun directoryForLibrary(library: TestedLibrary): File {
        val packageParts = library.component.module.group.split(".")
        return packageParts.fold(filesDir) { acc, part ->
            acc.resolve(part)
        }.resolve(library.component.module.name).resolve(library.component.version).also { it.mkdirs() }
    }

    private fun linkFileForIndex(index: Int, rootRange: Range): File {
        val parentDir = if (rootRange.children.isEmpty()) {
            indicesDir
        } else {
            rootRange.children.directoryForIndex(indicesDir, index)
        }

        parentDir.mkdirs()

        return parentDir.resolve(index.toString())
    }

    private fun List<Range>.directoryForIndex(parent: File, index: Int): File {
        val range = first { it.contains(index) }

        return if (range.children.isEmpty()) {
            parent.resolve(range.pathComponent)
        } else {
            range.children.directoryForIndex(parent.resolve(range.pathComponent), index)
        }
    }

    data class Range(
        val kotlinRange: IntRange,
        val children: List<Range>,
    ) {

        val pathComponent: String = "${kotlinRange.first}..${kotlinRange.last}"

        fun contains(index: Int) = kotlinRange.contains(index)

        companion object {

            operator fun invoke(count: Int): Range = Range(start = 0, endInclusive = count - 1, count.toString().length)

            private operator fun invoke(start: Int, endInclusive: Int, order: Int): Range {
                val kotlinRange = start.rangeTo(endInclusive)
                val children = if (order > 1) {
                    val step = 10.0.pow(order - 1).toInt()
                    kotlinRange.step(step).map {
                        Range(it, it + step - 1, order - 1)
                    }
                } else {
                    emptyList()
                }

                return Range(
                    kotlinRange = kotlinRange,
                    children = children,
                )
            }
        }
    }
}

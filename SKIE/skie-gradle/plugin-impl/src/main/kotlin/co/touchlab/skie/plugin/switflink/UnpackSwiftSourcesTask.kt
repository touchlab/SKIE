package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import co.touchlab.skie.util.collisionFreeIdentifier
import co.touchlab.skie.util.file.deleteEmptyDirectoriesRecursively
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class UnpackSwiftSourcesTask : DefaultTask() {

    @get:InputFiles
    abstract val klibs: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: Property<File>

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    @TaskAction
    fun execute() {
        val temporaryDirectory = temporaryDir.resolve("extractedSwiftSources")
        temporaryDirectory.deleteRecursively()

        getKlibsWithUniqueNames().forEach { (klib, uniqueName) ->
            unpackKlib(klib, uniqueName, temporaryDirectory)
        }

        temporaryDirectory.deleteEmptyDirectoriesRecursively()
        temporaryDirectory.mkdirs()

        // These files are compiled by the Swift compiler which only checks timestamps when evaluating incremental builds.
        temporaryDirectory.syncDirectoryContentIfDifferent(output.get())
    }

    private fun getKlibsWithUniqueNames(): List<Pair<File, String>> {
        val usedNames = mutableSetOf<String>()

        fun File.getUniqueName(): String {
            val uniqueName = nameWithoutExtension.collisionFreeIdentifier(usedNames)

            usedNames.add(uniqueName)

            return uniqueName
        }

        return klibs.map { it to it.getUniqueName() }
    }

    private fun unpackKlib(klib: File, uniqueName: String, temporaryDirectory: File) {
        if (!klib.exists()) {
            return
        }

        fileSystemOperations.copy {
            from(archiveOperations.zipTree(klib)) {
                include("${SwiftBundlingConfigurator.KLIB_SKIE_SWIFT_DIRECTORY}/**/*.swift")
            }

            eachFile {
                val basePath = relativePath.pathString.removePrefix(SwiftBundlingConfigurator.KLIB_SKIE_SWIFT_DIRECTORY)
                    .substringBeforeLast("/")
                    .removePrefix("/")

                val basePathNameComponent = if (basePath.isNotEmpty()) "${basePath.replace("/", ".")}." else ""

                val updatedFileName = "bundled.$uniqueName.$basePathNameComponent$name"

                val filePath = "$uniqueName/$basePath/$updatedFileName"

                relativePath = RelativePath(true, filePath)
            }

            into(temporaryDirectory)
        }
    }
}

package co.touchlab.skie.plugin.switflink

import co.touchlab.skie.util.cache.syncDirectoryContentIfDifferent
import co.touchlab.skie.util.collisionFreeIdentifier
import co.touchlab.skie.util.file.deleteEmptyDirectoriesRecursively
import co.touchlab.skie.util.file.isKlib
import co.touchlab.skie.util.file.isSwift
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class UnpackSwiftSourcesTask : DefaultTask() {

    @get:Input
    abstract val dependencies: ListProperty<File>

    @get:InputFiles
    protected val klibs: FileCollection
        get() = objects.fileCollection().also { files ->
            dependencies.get().filter { it.isKlib }.forEach {
                files.from(it)
            }
        }

    @get:InputFiles
    protected val unpackedKlibSwiftFiles: FileCollection
        get() = objects.fileCollection().also { files ->
            dependencies.get().filter { it.isDirectory }.forEach {
                files.from(
                    objects.fileTree().from(it).filter { it.isSwift },
                )
            }
        }

    @get:OutputDirectory
    abstract val output: Property<File>

    @get:Inject
    abstract val objects: ObjectFactory

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

        return dependencies.get().map { it to it.getUniqueName() }
    }

    private fun unpackKlib(klib: File, uniqueName: String, temporaryDirectory: File) {
        if (!klib.exists()) {
            return
        }

        fileSystemOperations.copy {
            val source = if (klib.isKlib) {
                archiveOperations.zipTree(klib)
            } else {
                klib
            }
            from(source) {
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

package co.touchlab.skie.plugin.util

import javax.inject.Inject
import org.gradle.api.file.FileSystemOperations

interface InjectedFileSystemOperations {

    @get:Inject
    val fileSystemOperations: FileSystemOperations
}

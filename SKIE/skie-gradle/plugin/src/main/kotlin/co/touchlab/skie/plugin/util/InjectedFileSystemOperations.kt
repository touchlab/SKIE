package co.touchlab.skie.plugin.util

import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

interface InjectedFileSystemOperations {
    @get:Inject
    val fileSystemOperations: FileSystemOperations
}

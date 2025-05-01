package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.structure.RootDirectory
import java.io.File

class SkieApplicationSupportDirectory(rootDirectory: File) : RootDirectory(rootDirectory) {

    val analyticsId: File = directory.resolve("analytics-id")
}

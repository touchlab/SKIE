package co.touchlab.skie.util.directory

import co.touchlab.skie.util.directory.structure.Directory
import co.touchlab.skie.util.directory.structure.PermanentDirectory
import co.touchlab.skie.util.directory.structure.RootDirectory
import java.io.File

class SkieCompilationDirectory(
    rootDirectory: File,
) : RootDirectory(rootDirectory) {

    val swift: Swift = Swift(this)

    class Swift(parent: Directory) : PermanentDirectory(parent, "swift") {

        val bundled: Bundled = Bundled(this)

        class Bundled(parent: Directory) : PermanentDirectory(parent, "bundled")
    }
}

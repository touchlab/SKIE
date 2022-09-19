package co.touchlab.swiftgen.acceptancetests.framework

import java.nio.file.Path
import kotlin.io.path.createDirectories

class TempFileSystemFactory(val tempDirectory: Path) {

    init {
        tempDirectory.createDirectories()
    }

    fun create(test: TestNode.Test): TempFileSystem =
        TempFileSystem(test.testTempDirectory(this))
}
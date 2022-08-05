package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import java.nio.file.Path

class TempFileSystemFactory(private val testConfiguration: TestConfiguration) {

    private val fileSystems = mutableMapOf<TestNode.Test, TempFileSystem>()

    fun create(test: TestNode.Test): TempFileSystem =
        TempFileSystem(testConfiguration)
            .also { fileSystems[test] = it }
}
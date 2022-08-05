package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import java.nio.file.Path

class TempFileSystem(private val testConfiguration: TestConfiguration) {

    private val files = mutableMapOf<String, Path>()
    private val directories = mutableMapOf<String, Path>()

    fun createFile(debugName: String, suffix: String? = null): Path =
        testConfiguration.tempfile(suffix = suffix).toPath()
            .also { files[debugName] = it }

    fun createDirectory(debugName: String): Path = testConfiguration.tempdir().toPath()
        .also { directories[debugName] = it }
}
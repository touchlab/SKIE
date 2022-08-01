package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import java.nio.file.Path

class TempFileSystem(private val testConfiguration: TestConfiguration) {

    fun createFile(suffix: String? = null): Path = testConfiguration.tempfile(suffix = suffix).toPath()

    fun createDirectory(): Path = testConfiguration.tempdir().toPath()
}
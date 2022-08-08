package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.core.TestConfiguration

class TempFileSystemFactory(private val testConfiguration: TestConfiguration) {

    private val fileSystems = mutableMapOf<TestNode.Test, TempFileSystem>()

    @Synchronized
    fun create(test: TestNode.Test): TempFileSystem =
        TempFileSystem(testConfiguration)
            .also { fileSystems[test] = it }
}
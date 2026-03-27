package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.plugin.libraries.ExternalLibraryTest
import java.nio.file.Path

class DefaultLockfileUpdater(
    outputPath: Path,
) : BaseLockfileUpdater(outputPath) {

    @Synchronized
    override fun addSkippedLibraries(executedTests: List<ExternalLibraryTest>, allTests: List<ExternalLibraryTest>) {
        val skippedLibraries = allTests.map { it.library } - executedTests.map { it.library }.toSet()

        addAllLibraries(skippedLibraries)
    }
}

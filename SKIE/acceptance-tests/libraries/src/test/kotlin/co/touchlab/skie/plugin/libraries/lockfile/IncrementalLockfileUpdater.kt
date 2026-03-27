package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.plugin.libraries.ExternalLibraryTest
import java.nio.file.Path

class IncrementalLockfileUpdater(
    outputPath: Path,
) : BaseLockfileUpdater(outputPath) {

    init {
        LockfileProvider.getLockfile(outputPath)?.let { lockfile ->
            addAllLibraries(lockfile.libraries)
        }
    }

    override fun addSkippedLibraries(executedTests: List<ExternalLibraryTest>, allTests: List<ExternalLibraryTest>) {
        // Skipped libraries are not added in incremental mode
    }
}

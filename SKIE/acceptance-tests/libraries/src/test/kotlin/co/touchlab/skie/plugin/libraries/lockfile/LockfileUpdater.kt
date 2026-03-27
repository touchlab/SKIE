package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.plugin.libraries.ExternalLibraryTest
import co.touchlab.skie.plugin.libraries.library.Artifacts

interface LockfileUpdater {

    fun add(test: ExternalLibraryTest, result: TestResultWithLogs, kotlinArtifacts: Result<Artifacts>)

    fun addSkippedLibraries(executedTests: List<ExternalLibraryTest>, allTests: List<ExternalLibraryTest>)

    fun writeToDisk()
}

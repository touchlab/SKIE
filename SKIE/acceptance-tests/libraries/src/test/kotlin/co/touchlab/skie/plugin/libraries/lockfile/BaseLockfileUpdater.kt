package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.plugin.libraries.ExternalLibraryTest
import co.touchlab.skie.plugin.libraries.library.Artifacts
import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.writeText

abstract class BaseLockfileUpdater(
    protected val outputPath: Path,
) : LockfileUpdater {

    private val includeFailedTestsInLockfile = "includeFailedTestsInLockfile" in TestProperties

    private val testedLibrariesByCoordinate = mutableMapOf<String, TestedLibrary>()

    private val json = Json {
        prettyPrint = true
    }

    @Synchronized
    override fun add(test: ExternalLibraryTest, result: TestResultWithLogs, kotlinArtifacts: Result<Artifacts>) {
        if (includeFailedTestsInLockfile || test.expectedResult.hasSucceeded(result)) {
            val unwrappedArtifacts = kotlinArtifacts.getOrNull()

            val updatedLibrary = if (unwrappedArtifacts != null) {
                test.library.updatedWithArtifacts(unwrappedArtifacts)
            } else {
                test.library
            }

            addLibrary(updatedLibrary)
        }
    }

    protected fun addLibrary(library: TestedLibrary) {
        testedLibrariesByCoordinate[library.component.coordinate] = library
    }

    protected fun addAllLibraries(libraries: List<TestedLibrary>) {
        libraries.forEach {
            addLibrary(it)
        }
    }

    @Synchronized
    override fun writeToDisk() {
        val lockfile = Lockfile(
            libraries = testedLibrariesByCoordinate
                .entries
                .sortedBy { it.key }
                .mapIndexed { index, (coordinate, library) ->
                    Lockfile.Library(
                        index = index,
                        coordinate = coordinate,
                        dependencies = library.dependencies.map { it.coordinate }.sorted(),
                    )
                },
        )

        val output = json.encodeToString(lockfile)

        outputPath.writeText(output)
    }

    private fun TestedLibrary.updatedWithArtifacts(artifacts: Artifacts): TestedLibrary =
        this.copy(
            component = artifacts.all.firstOrNull { it.component.module == component.module }?.component ?: this.component,
            dependencies = artifacts.all.filter { it.component.module != component.module }.map { it.component },
        )
}

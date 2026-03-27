package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.library.Component
import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

object LockfileProvider {

    fun getLockfile(lockfilePath: Path = Path(TestBuildConfig.LOCKFILE_PATH)): ParsedLockfile? {
        val lockfile = lockfilePath.takeIf { it.exists() } ?: return null

        val libraries = parseLockfile(lockfile)

        return ParsedLockfile(libraries)
    }

    private fun parseLockfile(lockfile: Path): List<TestedLibrary> =
        lockfile.readText()
            .let { Json.decodeFromString<Lockfile>(it) }
            .libraries
            .mapIndexed { index, library ->
                TestedLibrary(
                    index = index,
                    component = Component(library.coordinate),
                    dependencies = library.dependencies.map { Component(it) },
                )
            }
}

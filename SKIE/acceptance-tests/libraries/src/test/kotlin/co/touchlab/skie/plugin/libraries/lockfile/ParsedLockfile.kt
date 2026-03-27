package co.touchlab.skie.plugin.libraries.lockfile

import co.touchlab.skie.plugin.libraries.library.TestedLibrary

@JvmInline
value class ParsedLockfile(
    val libraries: List<TestedLibrary>,
)

package co.touchlab.skie.plugin.libraries.library

import java.nio.file.Path

data class Artifact(
    val component: Component,
    val path: Path,
)

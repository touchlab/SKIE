package co.touchlab.skie.plugin.relativepaths

import co.touchlab.skie.plugin.SkieTarget

fun SkieTarget.configureDebugPrefixMap() {
    if (!project.isRelativeSourcePathsPreviewEnabled) {
        return
    }

    addFreeCompilerArgs(
        "-Xdebug-prefix-map=${project.rootDir.absolutePath}=.",
    )
}

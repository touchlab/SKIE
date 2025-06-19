package co.touchlab.skie.plugin.relativepaths

import co.touchlab.skie.plugin.SkieTarget

fun SkieTarget.configureDebugPrefixMap() {
    if (!project.isRelativeSourcePathsPreviewEnabled) {
        return
    }

    addFreeCompilerArgsImmediately(
        "-Xdebug-prefix-map=${project.rootDir.absolutePath}=."
    )
}

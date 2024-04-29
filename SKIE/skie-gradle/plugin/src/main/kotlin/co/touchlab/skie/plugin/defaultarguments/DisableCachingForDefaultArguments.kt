package co.touchlab.skie.plugin.defaultarguments

import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.presetName

fun SkieTarget.disableCachingIfNeeded() {
    if (project.areDefaultArgumentsInCachedLibrariesEnabled) {
        project.extensions.extraProperties.set("kotlin.native.cacheKind.${konanTarget.presetName}", "none")
    }
}

internal val Project.areDefaultArgumentsInCachedLibrariesEnabled: Boolean
    get() = project.skieExtension.features.defaultArgumentsInExternalLibraries.get()

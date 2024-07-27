package co.touchlab.skie.plugin.defaultarguments

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.configuration.skieExtension
import org.gradle.api.Project

fun SkieTarget.disableCachingIfNeeded() {
    if (project.areDefaultArgumentsInCachedLibrariesEnabled) {
        project.extensions.extraProperties.set("kotlin.native.cacheKind.${konanTarget.presetName}", "none")
    }
}

val Project.areDefaultArgumentsInCachedLibrariesEnabled: Boolean
    get() = project.skieExtension.features.defaultArgumentsInExternalLibraries.get()

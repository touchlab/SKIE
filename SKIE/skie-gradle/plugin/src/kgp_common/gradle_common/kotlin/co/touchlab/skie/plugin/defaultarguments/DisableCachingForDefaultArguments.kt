package co.touchlab.skie.plugin.defaultarguments

import co.touchlab.skie.plugin.configuration.skieExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

fun KotlinNativeLink.disableCachingIfNeeded() {
    if (project.areDefaultArgumentsInCachedLibrariesEnabled) {
        project.extensions.extraProperties.set("kotlin.native.cacheKind.${binary.target.konanTarget.presetName}", "none")
    }
}

internal val Project.areDefaultArgumentsInCachedLibrariesEnabled: Boolean
    get() = project.skieExtension.features.defaultArgumentsInExternalLibraries.get()

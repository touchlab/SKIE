package co.touchlab.skie.plugin.util

import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

// TODO Decide if this should be public or internal

val KotlinNativeLink.skieDirectories: SkieDirectories
    get() = SkieDirectories(
        project.layout.buildDirectory.dir("skie/${binary.name}/${binary.target.targetName}").get().asFile,
    )

val KotlinNativeLink.skieBuildDirectory: SkieBuildDirectory
    get() = skieDirectories.buildDirectory

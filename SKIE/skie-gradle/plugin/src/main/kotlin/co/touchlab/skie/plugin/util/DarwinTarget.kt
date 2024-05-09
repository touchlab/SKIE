package co.touchlab.skie.plugin.util

import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.konan.target.KonanTarget

internal val FrameworkDescriptor.darwinTarget: DarwinTarget
    get() = target.darwinTarget

internal val KonanTarget.darwinTarget: DarwinTarget
    // We can't use `KotlinTarget` directly as the instance can differ when Gradle Configuration Cache is used
    get() = DarwinTarget.allTargets[name] ?: error("Unknown konan target: $this")

package co.touchlab.skie.plugin.util

import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.konan.target.KonanTarget

internal data class DarwinTarget(
    val konanTarget: KonanTarget,
    val targetTriple: TargetTriple,
    val sdk: String,
) {

    constructor(
        konanTarget: KonanTarget,
        targetTripleString: String,
        sdk: String,
    ) : this(konanTarget, TargetTriple.fromString(targetTripleString), sdk)

    companion object {

        val allTargets: Map<String, DarwinTarget> = getAllSupportedDarwinTargets().associateBy { it.konanTarget.name }
    }
}

internal expect fun getAllSupportedDarwinTargets(): List<DarwinTarget>

internal val FrameworkDescriptor.darwinTarget: DarwinTarget
    get() = target.darwinTarget

internal val KonanTarget.darwinTarget: DarwinTarget
    // We can't use `KotlinTarget` directly as the instance can differ when Gradle Configuration Cache is used
    get() = DarwinTarget.allTargets[name] ?: error("Unknown konan target: $this")

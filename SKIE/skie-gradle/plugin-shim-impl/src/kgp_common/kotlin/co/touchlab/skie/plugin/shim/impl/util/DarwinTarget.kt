package co.touchlab.skie.plugin.shim.impl.util

import co.touchlab.skie.util.TargetTriple
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.konan.target.KonanTarget

data class DarwinTarget(
    val konanTarget: KonanTarget,
    val targetTriple: TargetTriple,
    val sdk: String,
) {

    constructor(
        konanTarget: KonanTarget,
        targetTripleString: String,
        sdk: String,
    ) : this(
        konanTarget,
        TargetTriple(targetTripleString), sdk,
    )

    companion object {

        val allTargets: Map<String, DarwinTarget> = getAllSupportedDarwinTargets().associateBy { it.konanTarget.name }
    }
}

expect fun getAllSupportedDarwinTargets(): List<DarwinTarget>

val FrameworkDescriptor.darwinTarget: DarwinTarget
    get() = target.darwinTarget

val KonanTarget.darwinTarget: DarwinTarget
    // We can't use `KotlinTarget` directly as the instance can differ when Gradle Configuration Cache is used
    get() = DarwinTarget.allTargets[name] ?: error("Unknown konan target: $this")

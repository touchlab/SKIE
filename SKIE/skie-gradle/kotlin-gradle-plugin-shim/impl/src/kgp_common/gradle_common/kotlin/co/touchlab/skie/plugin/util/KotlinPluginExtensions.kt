package co.touchlab.skie.plugin.util

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal val KotlinMultiplatformExtension.appleTargets: List<KotlinNativeTarget>
    get() = targets
        .mapNotNull { it as? KotlinNativeTarget }
        .filter { it.konanTarget.family.isAppleFamily }

internal val KotlinNativeTarget.frameworks: List<Framework>
    get() = binaries.filterIsInstance<Framework>()

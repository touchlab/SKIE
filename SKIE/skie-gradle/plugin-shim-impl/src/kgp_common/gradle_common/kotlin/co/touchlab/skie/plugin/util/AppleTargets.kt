package co.touchlab.skie.plugin.util

import org.gradle.api.NamedDomainObjectCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val KotlinMultiplatformExtension.appleTargets: NamedDomainObjectCollection<KotlinNativeTarget>
    get() = targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family.isAppleFamily }

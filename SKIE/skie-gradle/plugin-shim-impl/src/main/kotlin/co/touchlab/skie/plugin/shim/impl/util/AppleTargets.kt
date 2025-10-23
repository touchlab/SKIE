package co.touchlab.skie.plugin.shim.impl.util

import co.touchlab.skie.plugin.util.withType
import org.gradle.api.NamedDomainObjectCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val KotlinMultiplatformExtension.appleTargets: NamedDomainObjectCollection<KotlinNativeTarget>
    get() = targets.withType<KotlinNativeTarget>().matching { it.konanTarget.family.isAppleFamily }

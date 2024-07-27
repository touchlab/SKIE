package co.touchlab.skie.plugin.shim.impl.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val Project.kotlinMultiplatformExtension: KotlinMultiplatformExtension?
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

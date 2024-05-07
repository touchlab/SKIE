package co.touchlab.skie.plugin.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val Project.kotlinMultiplatformExtension: KotlinMultiplatformExtension?
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

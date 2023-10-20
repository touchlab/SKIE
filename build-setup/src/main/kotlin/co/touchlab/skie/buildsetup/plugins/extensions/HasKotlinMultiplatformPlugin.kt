package co.touchlab.skie.buildsetup.plugins.extensions

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

interface HasKotlinMultiplatformPlugin {

    val Project.kotlin: KotlinMultiplatformExtension
        get() = extensions.getByType()

    fun Project.kotlin(configure: KotlinMultiplatformExtension.() -> Unit) = extensions.configure(configure)
}

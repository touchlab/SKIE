package co.touchlab.skie.gradle.kotlin

import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class SkieKotlinMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyKotlinMultiplatformPlugin()
            setJvmToolchain()
        }
    }

    private fun Project.applyKotlinMultiplatformPlugin() {
        plugins.apply("org.jetbrains.kotlin.multiplatform")
    }

    private fun Project.setJvmToolchain() {
        extensions.configure(KotlinMultiplatformExtension::class.java) {
            jvmToolchain(libs.versions.java)
        }
    }
}

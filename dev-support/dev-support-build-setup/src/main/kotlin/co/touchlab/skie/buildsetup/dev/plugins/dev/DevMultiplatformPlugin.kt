package co.touchlab.skie.buildsetup.dev.plugins.dev

import co.touchlab.skie.buildsetup.main.plugins.base.BasePlugin
import co.touchlab.skie.buildsetup.util.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class DevMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BasePlugin>()
        apply<KotlinMultiplatformPluginWrapper>()

        configureJvmToolchain()
    }

    private fun Project.configureJvmToolchain() {
        plugins.withType<KotlinBasePluginWrapper>().configureEach {
            extensions.configure<KotlinBaseExtension> {
                jvmToolchain(libs.versions.jvmToolchain)
            }
        }
    }
}

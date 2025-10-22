@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.base

import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import co.touchlab.skie.gradle.util.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.plugin.DefaultKotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

abstract class BaseKotlin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        group = "co.touchlab.skie"
        version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }

        configureCompilerVersionAttribute()
        configureJvmToolchain()
        configureTargetJvm()
        configureJUnitTests()
    }

    private fun Project.configureCompilerVersionAttribute() {
        // WIP
        plugins.withType<DefaultKotlinBasePlugin>().configureEach {
            KotlinCompilerVersionAttribute.registerIn(project.dependencies, pluginVersion)
        }
    }

    private fun Project.configureJvmToolchain() {
        plugins.withType<KotlinBasePluginWrapper>().configureEach {
            extensions.configure<KotlinTopLevelExtension> {
                jvmToolchain(libs.versions.jvmToolchain)
            }
        }
    }

    private fun Project.configureTargetJvm() {
        plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType(KotlinJvmTarget::class).configureEach {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.toString()))

                        freeCompilerArgs.addAll(
                            "-Xjdk-release=${libs.versions.jvmTarget}",
                        )
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper>().configureEach {
            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.toString()))

                    freeCompilerArgs.addAll(
                        "-Xjdk-release=${libs.versions.jvmTarget}",
                    )
                }
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(libs.versions.jvmTarget)
        }
    }

    private fun Project.configureJUnitTests() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

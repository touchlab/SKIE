@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.base

import co.touchlab.skie.buildsetup.util.libs
import co.touchlab.skie.buildsetup.util.version.KotlinVersionAttribute
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.DefaultKotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

abstract class BaseKotlinPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        group = "co.touchlab.skie"
        version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }

        configureCompilerVersionAttribute()
        configureJvmToolchain()
        configureTargetJvm()
        configureTargetJvmAttribute()
        configureJUnitTests()
    }

    private fun Project.configureCompilerVersionAttribute() {
        plugins.withType<DefaultKotlinBasePlugin>().configureEach {
            val supportedKotlinVersions = SupportedKotlinVersionProvider.getSupportedKotlinVersions(project)

            KotlinVersionAttribute.registerIn(dependencies, supportedKotlinVersions)
        }
    }

    private fun Project.configureJvmToolchain() {
        plugins.withType<KotlinBasePluginWrapper>().configureEach {
            extensions.configure<KotlinBaseExtension> {
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

    private fun Project.configureTargetJvmAttribute() {
        plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType(KotlinJvmTarget::class).configureEach {
                    configurations.named(apiElementsConfigurationName).configure {
                        attributes {
                            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, libs.versions.jvmTarget)
                        }
                    }

                    configurations.named(runtimeElementsConfigurationName).configure {
                        attributes {
                            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, libs.versions.jvmTarget)
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureJUnitTests() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

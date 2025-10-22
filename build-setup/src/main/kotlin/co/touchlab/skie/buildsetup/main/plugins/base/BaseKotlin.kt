package co.touchlab.skie.buildsetup.main.plugins.base

import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import co.touchlab.skie.gradle.util.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class BaseKotlin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        group = "co.touchlab.skie"
        version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }

        configureJvmToolchain()
        configureJUnitTests()
    }

    private fun Project.configureJvmToolchain() {
        plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            KotlinCompilerVersionAttribute.registerIn(project.dependencies, pluginVersion)

            extensions.configure<KotlinMultiplatformExtension> {
                jvmToolchain(libs.versions.java)

                targets.all {
                    mavenPublication {
                        attributes {
                            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, libs.versions.java)
                        }
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper>().configureEach {
            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(libs.versions.java)
            }
        }
    }

    private fun Project.configureJUnitTests() {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

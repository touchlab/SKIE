@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as GradleDslKotlinVersion

abstract class UtilityMinimumTargetKotlinVersionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val minimumVersion = SupportedKotlinVersionProvider.getMinimumSupportedKotlinVersion(project)

        setMinimumTargetKotlinVersion(project, minimumVersion)
    }

    companion object {

        fun setMinimumTargetKotlinVersion(project: Project, version: KotlinToolingVersion) {
            val minimumKotlinVersion = GradleDslKotlinVersion.fromVersion("${version.major}.${version.minor}")

            project.plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
                project.extensions.configure<KotlinMultiplatformExtension> {
                    compilerOptions {
                        apiVersion.set(minimumKotlinVersion)
                        languageVersion.set(minimumKotlinVersion)
                    }
                }
            }

            project.plugins.withType<KotlinPluginWrapper>().configureEach {
                project.extensions.configure<KotlinJvmProjectExtension> {
                    compilerOptions {
                        apiVersion.set(minimumKotlinVersion)
                        languageVersion.set(minimumKotlinVersion)
                    }
                }
            }
        }
    }
}

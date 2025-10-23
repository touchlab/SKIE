@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.main.plugins.utility

import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
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

abstract class UtilityMinimumTargetKotlinVersion : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val minimumVersion = KotlinToolingVersionProvider.getMinimumSupportedKotlinToolingVersion(project)

        val minimumKotlinVersion = GradleDslKotlinVersion.fromVersion("${minimumVersion.major}.${minimumVersion.minor}")

        plugins.withType<KotlinMultiplatformPluginWrapper>().configureEach {
            extensions.configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    apiVersion.set(minimumKotlinVersion)
                    languageVersion.set(minimumKotlinVersion)
                }
            }
        }

        plugins.withType<KotlinPluginWrapper>().configureEach {
            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    apiVersion.set(minimumKotlinVersion)
                    languageVersion.set(minimumKotlinVersion)
                }
            }
        }
    }
}

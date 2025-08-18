package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

abstract class UtilityOptInExperimentalCompilerApi : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val annotations = listOf(
            "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
            "org.jetbrains.kotlin.backend.konan.InternalKotlinNativeApi",
        )

        plugins.withType<KotlinMultiplatformPluginWrapper> {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        annotations.forEach {
                            optIn(it)
                        }
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper> {
            project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
                annotations.forEach {
                    compilerOptions.freeCompilerArgs.add("-opt-in=$it")
                }
            }
        }
    }
}

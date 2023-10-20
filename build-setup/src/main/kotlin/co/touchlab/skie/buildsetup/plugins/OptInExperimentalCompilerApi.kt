package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

abstract class OptInExperimentalCompilerApi : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        plugins.withType<KotlinMultiplatformPluginWrapper> {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper> {
            project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
                compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
            }
        }
    }
}

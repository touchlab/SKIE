package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

abstract class UtilityExperimentalContextReceivers : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        plugins.withType<KotlinMultiplatformPluginWrapper> {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        enableLanguageFeature("ContextReceivers")
                    }
                }
            }
        }

        plugins.withType<KotlinPluginWrapper> {
            tasks.withType<KotlinCompilationTask<*>>().configureEach {
                compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
            }
        }
    }
}

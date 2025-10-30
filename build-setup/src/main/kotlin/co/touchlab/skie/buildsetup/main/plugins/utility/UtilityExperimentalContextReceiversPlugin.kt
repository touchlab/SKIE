package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

abstract class UtilityExperimentalContextReceiversPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        plugins.withType<KotlinBasePluginWrapper>().configureEach {
            extensions.configure<KotlinBaseExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        enableLanguageFeature("ContextReceivers")
                    }
                }

                if (this is HasConfigurableKotlinCompilerOptions<*>) {
                    compilerOptions {
                        freeCompilerArgs.addAll(
                            "-Xwarning-level=CONTEXT_RECEIVERS_DEPRECATED:disabled",
                        )
                    }
                }
            }
        }
    }
}

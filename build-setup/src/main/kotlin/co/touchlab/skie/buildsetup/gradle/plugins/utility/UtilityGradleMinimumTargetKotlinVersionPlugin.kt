package co.touchlab.skie.buildsetup.gradle.plugins.utility

import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.minGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class UtilityGradleMinimumTargetKotlinVersionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val minimumVersion = KotlinToolingVersion(minGradleVersion().embeddedKotlin)

        UtilityMinimumTargetKotlinVersionPlugin.setMinimumTargetKotlinVersion(project, minimumVersion)

        project.plugins.withType<KotlinPluginWrapper>().configureEach {
            project.extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        "-Xsuppress-version-warnings",
                    )
                }
            }
        }
    }
}

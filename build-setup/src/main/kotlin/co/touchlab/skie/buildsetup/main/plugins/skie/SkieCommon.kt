package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.version.KotlinToolingVersionProvider
import co.touchlab.skie.gradle.util.compileOnly
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as GradleDslKotlinVersion

class SkieCommon : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<KotlinPluginWrapper>()

        configureTargetKotlinVersion()
        configureDependencies()
    }

    private fun Project.configureTargetKotlinVersion() {
        val minimumVersion = KotlinToolingVersionProvider.getMinimumSupportedKotlinToolingVersion(project)

        val minimumKotlinVersion = GradleDslKotlinVersion.fromVersion("${minimumVersion.major}.${minimumVersion.minor}")

        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                apiVersion.set(minimumKotlinVersion)
                languageVersion.set(minimumKotlinVersion)
            }
        }
    }

    private fun Project.configureDependencies() {
        dependencies {
            compileOnly(kotlin("stdlib"))
        }
    }
}

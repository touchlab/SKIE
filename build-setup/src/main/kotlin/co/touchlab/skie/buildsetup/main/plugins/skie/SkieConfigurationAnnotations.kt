package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class SkieConfigurationAnnotations : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<KotlinMultiplatformPluginWrapper>()

        configureWeakStdlibDependency()
    }

    /*
     * We add a dependency on stdlib as `compileOnly` as we don't want to override users'
     * stdlib version in their projects.
     */
    private fun Project.configureWeakStdlibDependency() {
        extensions.configure<KotlinMultiplatformExtension> {
        }

        afterEvaluate {
            extensions.getByType<KotlinMultiplatformExtension>().apply {
                sourceSets["jvmMain"].dependencies {
                    compileOnly(kotlin("stdlib"))
                }

                sourceSets["jsMain"].dependencies {
                    compileOnly(kotlin("stdlib-js"))
                }
            }
        }
    }
}

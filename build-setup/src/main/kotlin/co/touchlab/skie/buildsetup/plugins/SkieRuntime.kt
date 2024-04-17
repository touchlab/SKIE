package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class SkieRuntime : Plugin<Project> {

    /*
     * We add dependency on stdlib as `compileOnly` as we don't want to override users'
     * stdlib version in their projects. We also can't use `MultiDimensionTargetExtension` here,
     * as it's not to be used with user-consumable dependencies.
     */
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<KotlinMultiplatformPluginWrapper>()

        extensions.configure<KotlinMultiplatformExtension> {
            val commonMain by sourceSets.getting {
                dependencies {
                    compileOnly(kotlin("stdlib-common"))
                }
            }
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

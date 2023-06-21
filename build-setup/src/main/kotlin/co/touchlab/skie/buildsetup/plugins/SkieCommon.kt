package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.implementation
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class SkieCommon: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<KotlinMultiplatformPluginWrapper>()

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)

            jvm()

            sourceSets {
                val commonMain by getting {
                    dependencies {
                        compileOnly(kotlin("stdlib"))
                    }
                }

                val commonTest by getting {
                    dependencies {
                        implementation(kotlin("stdlib"))
                        implementation(libs.bundles.testing.jvm)
                    }
                }
            }
        }
    }
}

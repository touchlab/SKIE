package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class SkieCommon : Plugin<Project> {

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

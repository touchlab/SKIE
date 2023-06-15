package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.getValue
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class SkieRuntime: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<KotlinMultiplatformPluginWrapper>()

        extensions.configure<KotlinMultiplatformExtension> {
            val commonMain by sourceSets.getting {
                dependencies {
                    implementation(kotlin("stdlib"))
                }
            }
        }
    }
}

package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieServer : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<KotlinPluginWrapper>()

        dependencies {
            testImplementation(libs.bundles.testing.jvm)
        }
    }
}

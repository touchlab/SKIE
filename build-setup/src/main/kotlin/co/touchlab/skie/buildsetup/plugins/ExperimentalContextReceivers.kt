package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

class ExperimentalContextReceivers: Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        tasks.withType<KotlinCompile<*>>().configureEach {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
            }
        }
    }
}

package co.touchlab.skie.gradle.kotlin

import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class SkieKotlinJvmPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyKotlinJvmPlugin()
            setJvmToolchain()
            configureTests()
            configureContextReceivers()
            configureOptIn()

            // TODO Configure Java toolchain (https://docs.gradle.org/current/userguide/toolchains.html)
        }
    }

    private fun Project.applyKotlinJvmPlugin() {
        plugins.apply("org.jetbrains.kotlin.jvm")
    }

    private fun Project.setJvmToolchain() {
        extensions.configure(KotlinJvmProjectExtension::class.java) {
            jvmToolchain(libs.versions.java)
        }
    }

    private fun Project.configureTests() {
        tasks.withType(Test::class.java).configureEach {
            useJUnitPlatform()
        }

        dependencies {
            testImplementation(libs.bundles.testing.jvm)
        }
    }

    private fun Project.configureContextReceivers() {
        val extension = extensions.create("skieJvm", JvmExtension::class.java)

        afterEvaluate {
            if (extension.areContextReceiversEnabled.get()) {
                tasks.withType(KotlinCompile::class.java).configureEach {
                    kotlinOptions {
                        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
                    }
                }
            }
        }
    }

    private fun Project.configureOptIn() {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
            compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        }
    }
}

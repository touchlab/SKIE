package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.kotlinToolingVersions
import co.touchlab.skie.gradle.version.setupSourceSets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class SkieCompiler: Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        apply<KotlinMultiplatformPluginWrapper>()

        group = "co.touchlab.skie"

        KotlinCompilerVersion.registerIn(project)

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)

            val kotlinVersions = project.kotlinToolingVersions()
            setupSourceSets(
                matrix = kotlinVersions,
                configureTarget = { cell ->
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(cell.toString()))
                    }
                },
                configureSourceSet = {
                    val kotlinVersion = target.baseValue.toString()

                    addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
                    addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
                    addWeakDependency("org.jetbrains.kotlin:kotlin-native-compiler-embeddable", configureVersion(kotlinVersion))
                },
            )
        }

        tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        }
    }
}

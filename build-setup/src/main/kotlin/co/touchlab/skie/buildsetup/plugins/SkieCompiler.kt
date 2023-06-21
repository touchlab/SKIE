package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.*
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
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
        apply<SkieBase>()
        apply<KotlinMultiplatformPluginWrapper>()
        apply<MultiDimensionTargetPlugin>()

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions.add(kotlinToolingVersionDimension())

            createTarget { target ->
                jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.value

                addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
                addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
                addWeakDependency("org.jetbrains.kotlin:kotlin-native-compiler-embeddable", configureVersion(kotlinVersion))
            }
        }

        tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        }
    }
}

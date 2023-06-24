package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.util.kotlinNativeCompilerHome
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import co.touchlab.skie.gradle.version.KotlinToolingVersionComponent
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader
import org.jetbrains.kotlin.konan.CompilerVersion
import java.io.File

abstract class DevAcceptanceTestsFramework: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()
        apply<ExperimentalContextReceivers>()
        apply<DevBuildconfig>()

        extensions.configure<BuildConfigExtension> {
            generator(
                ExpectActualBuildConfigGenerator(
                    isActualImplementation = false,
                    internalVisibility = false,
                )
            )

            buildConfigField(
                type = "String",
                name = "KONAN_HOME",
                value = "",
            )
            buildConfigField(
                type = "String",
                name = "RESOURCES",
                value = "",
            )
            buildConfigField(
                type = "java.nio.file.Path",
                name = "LICENSE_PATH",
                value = "",
            )
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension()) { target ->
                val kotlinTarget = jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }

                extensions.configure<BuildConfigExtension> {
                    sourceSets.named(target.name + "Main").configure {
                        generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
                        className.set("BuildConfig")

                        buildConfigField(
                            type = "String",
                            name = "KONAN_HOME",
                            value = "\"${project.kotlinNativeCompilerHome(target.kotlinToolingVersion).path}\"",
                        )

                        buildConfigField(
                            type = "String",
                            name = "RESOURCES",
                            value = kotlinTarget.compilations.named("main").flatMap {
                                tasks.named<ProcessResources>(it.processResourcesTaskName)
                            }.map {
                                "\"${it.destinationDir.absolutePath}\""
                            },
                        )

                        buildConfigField(
                            type = "java.nio.file.Path",
                            name = "LICENSE_PATH",
                            value = "Path.of(\"${rootProject.layout.projectDirectory.dir("licenses").file("tests.jwt").asFile.absolutePath}\")",
                        )
                    }
                }

                kotlinTarget
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.value

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    weak("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")

                    testOnly(libs.bundles.testing.jvm)
                }
            }
        }
    }

    private fun Project.kotlinNativeCompilerHome(kotlinVersion: KotlinToolingVersionComponent): File {
        return NativeCompilerDownloader(project, CompilerVersion.fromString(kotlinVersion.value))
            .also { it.downloadIfNeeded() }
            .compilerDirectory
    }
}

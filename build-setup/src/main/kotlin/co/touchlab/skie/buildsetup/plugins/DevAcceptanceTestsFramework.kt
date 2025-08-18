package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfig
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityExperimentalContextReceivers
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApi
import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import co.touchlab.skie.gradle.util.kotlinNativeCompilerHome
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.withKotlinNativeCompilerEmbeddableDependency
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.language.jvm.tasks.ProcessResources

abstract class DevAcceptanceTestsFramework : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<MultiDimensionTargetPlugin>()
        apply<UtilityOptInExperimentalCompilerApi>()
        apply<UtilityExperimentalContextReceivers>()
        apply<UtilityBuildConfig>()

        extensions.configure<BuildConfigExtension> {
            generator(
                ExpectActualBuildConfigGenerator(
                    isActualImplementation = false,
                    internalVisibility = false,
                ),
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
                        attribute(KotlinCompilerVersionAttribute.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }

                project.extensions.configure<BuildConfigExtension> {
                    sourceSets.named(target.name + "Main").configure {
                        generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
                        className.set("BuildConfig")

                        buildConfigField(
                            type = "String",
                            name = "KONAN_HOME",
                            value = "\"${project.kotlinNativeCompilerHome(target.kotlinToolingVersion.primaryVersion).path}\"",
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
                val kotlinVersion = sourceSet.kotlinToolingVersion.primaryVersion

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")

                    withKotlinNativeCompilerEmbeddableDependency(kotlinVersion, isTarget = sourceSet.isTarget) {
                        weak(it)
                    }

                    testOnly(libs.bundles.testing.jvm)
                }

                dependencies {
                    implementation(files(project.kotlinNativeCompilerHome(kotlinVersion).resolve("konan/lib/trove4j.jar")))
                }
            }
        }
    }
}

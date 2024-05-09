import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.gradleApiVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyCoordinate
import co.touchlab.skie.gradle.util.gradlePluginApi
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle")
    id("skie.publishable")

    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin Impl"
    description = "Internal implementation for SKIE Gradle plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.gradle.gradlePluginApi)
                api(projects.gradle.gradlePluginShimApi)
                implementation(projects.gradle.gradleSubPluginApi)
                implementation(projects.gradle.gradlePluginUtil)
                implementation(projects.common.analytics)
                implementation(projects.common.util)

                implementation(libs.ci.info)
                implementation(libs.jgit)
                implementation(libs.mixpanel)
            }

            kotlin.srcDirs(
                layout.projectDirectory.dir("src/common/kotlin-compiler-attribute"),
            )
        }
    }
}

buildConfig {
    generator(
        ExpectActualBuildConfigGenerator(
            isActualImplementation = false,
            internalVisibility = false,
        ),
    )

    buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", "")
    buildConfigField("co.touchlab.skie.plugin.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "")
    buildConfigField("String", "SKIE_VERSION", "")
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", "")
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_COORDINATE", "")
    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "")
}

multiDimensionTarget.configureSourceSet { sourceSet ->
    if (!sourceSet.isTarget || compilation.isTest) {
        return@configureSourceSet
    }

    buildConfig {
        this.sourceSets.named(kotlinSourceSet.name).configure {
            generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
            className.set("BuildConfig")

            val shimImpl = project.provider { projects.gradle.gradlePluginShimImpl.dependencyProject }
            buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", shimImpl.map { it.dependencyCoordinate.enquoted() })

            val kotlinToSkieKgpVersion = project.kotlinToolingVersionDimension().components
                .flatMap { versionComponent ->
                    versionComponent.supportedVersions.map { version ->
                        version to versionComponent.name
                    }
                }
                .joinToString { (version, name) ->
                    "${version.toString().enquoted()} to ${name.toString().enquoted()}"
                }

            buildConfigField("co.touchlab.skie.plugin.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "mapOf($kotlinToSkieKgpVersion)")

            buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")

            val kotlinPlugin = project.provider { projects.kotlinCompiler.kotlinCompilerLinkerPlugin.dependencyProject }
            buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })

            val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject }
            buildConfigField("String", "SKIE_KOTLIN_RUNTIME_COORDINATE", runtime.map { it.dependencyCoordinate.enquoted() })

            buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
        }
    }
}

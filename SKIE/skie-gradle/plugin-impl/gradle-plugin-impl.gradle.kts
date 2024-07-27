import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.gradleApiVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyCoordinate
import co.touchlab.skie.gradle.publish.dependencyModule
import co.touchlab.skie.gradle.util.gradlePluginApi
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle")
    // TODO: Remove
    id("skie.publishable")

    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin Impl"
    description = "Internal implementation for SKIE Gradle plugin."
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(projects.gradle.gradlePluginApi)
            api(projects.gradle.gradlePluginShimApi)
            implementation(projects.common.configuration.configurationDeclaration)
            implementation(projects.gradle.gradleSubPluginApi)
            implementation(projects.gradle.gradlePluginUtil)
            implementation(projects.common.analytics)
            implementation(projects.common.util)

            implementation(libs.ci.info)
            implementation(libs.jgit)
            implementation(libs.mixpanel)
        }

        kotlin.srcDirs(
            layout.projectDirectory.dir("src/main/kotlin-compiler-attribute"),
        )
    }
}

buildConfig {
//     generator(
//         ExpectActualBuildConfigGenerator(
//             isActualImplementation = false,
//             internalVisibility = false,
//         ),
//     )

    val shimImpl = project.provider { projects.gradle.gradlePluginShimImpl.dependencyProject }
    buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", shimImpl.map { it.dependencyCoordinate.enquoted() })
//     buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", "")
    val kotlinToSkieKgpVersion = project.kotlinToolingVersionDimension().components
        .flatMap { versionComponent ->
            versionComponent.supportedVersions.map { version ->
                version to versionComponent.name
            }
        }
        .joinToString { (version, name) ->
            "${version.toString().enquoted()} to ${name.toString().enquoted()}"
        }

    buildConfigField("co.touchlab.skie.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "mapOf($kotlinToSkieKgpVersion)")
//     buildConfigField("co.touchlab.skie.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "")
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")
//     buildConfigField("String", "SKIE_VERSION", "")
    val kotlinPlugin = project.provider { projects.kotlinCompiler.kotlinCompilerLinkerPlugin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })
//     buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", "")
    val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_MODULE", runtime.map { it.dependencyModule.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_GROUP", runtime.map { it.group.toString().enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_NAME", runtime.map { it.name.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_VERSION", runtime.map { it.version.toString().enquoted() })
//     buildConfigField("String", "SKIE_KOTLIN_RUNTIME_MODULE", "")
//     buildConfigField("String", "SKIE_KOTLIN_RUNTIME_COORDINATE", "")
//     buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "")
    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
}

// multiDimensionTarget.configureSourceSet { sourceSet ->
//     if (!sourceSet.isTarget || compilation.isTest) {
//         return@configureSourceSet
//     }
//
//     buildConfig {
//         this.sourceSets.named(kotlinSourceSet.name).configure {
//             generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
//             className.set("BuildConfig")
//
//
//
//
//
//
//
//
//
//
//         }
//     }
// }

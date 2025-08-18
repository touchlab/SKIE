import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.publish.dependencyCoordinate
import co.touchlab.skie.gradle.publish.dependencyModule
import co.touchlab.skie.gradle.util.gradlePluginApi
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle")
    // TODO: Remove
    id("skie.publishable")

    id("utility.build-config")
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

    buildConfigField("co.touchlab.skie.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "mapOf($kotlinToSkieKgpVersion)")
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")
    val kotlinPlugin = project.provider { projects.kotlinCompiler.kotlinCompilerLinkerPlugin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })
    val configurationAnnotations = project.provider { projects.common.configuration.configurationAnnotations.dependencyProject }
    buildConfigField("String", "SKIE_CONFIGURATION_ANNOTATIONS_MODULE", configurationAnnotations.map { it.dependencyModule.enquoted() })

    val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject }
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_MODULE", runtime.map { it.dependencyModule.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_GROUP", runtime.map { it.group.toString().enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_NAME", runtime.map { it.name.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_VERSION", runtime.map { it.version.toString().enquoted() })
    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
}

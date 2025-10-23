import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.buildsetup.util.dependencyModule
import co.touchlab.skie.buildsetup.util.dependencyCoordinate

plugins {
    id("gradle.common")
    id("utility.skie-publishable")

    id("utility.build-config")
}

skiePublishing {
    name = "SKIE Gradle Plugin Impl"
    description = "Internal implementation for SKIE Gradle plugin."
}

kotlin {
    sourceSets.main.configure {
        kotlin.srcDirs(
            layout.projectDirectory.dir("src/main/kotlin-tooling-version-gradle"),
            layout.projectDirectory.dir("src/main/kotlin-compiler-attribute"),
        )
    }
}

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

// WIP
buildConfig {
    val shimImpl = project.provider { project(projects.gradle.gradlePluginShimImpl.path) }
    buildConfigField("String", "SKIE_GRADLE_SHIM_IMPL_COORDINATE", shimImpl.map { it.dependencyCoordinate.enquoted() })

    val kotlinToSkieKgpVersion = KotlinToolingVersionProvider.getSupportedKotlinToolingVersions(project)
        .flatMap { supportedVersion ->
            supportedVersion.supportedVersions.map { version ->
                version to supportedVersion.name
            }
        }
        .joinToString { (version, name) ->
            "${version.toString().enquoted()} to ${name.toString().enquoted()}"
        }

    buildConfigField("co.touchlab.skie.util.StringMap", "KOTLIN_TO_SKIE_VERSION", "mapOf($kotlinToSkieKgpVersion)")
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")
    val kotlinPlugin = project.provider { project(projects.kotlinCompiler.kotlinCompilerLinkerPlugin.path) }
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })
    val configurationAnnotations = project.provider { project(projects.common.configuration.configurationAnnotations.path) }
    buildConfigField("String", "SKIE_CONFIGURATION_ANNOTATIONS_MODULE", configurationAnnotations.map { it.dependencyModule.enquoted() })

    val runtime = project.provider { project(projects.runtime.runtimeKotlin.path) }
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_MODULE", runtime.map { it.dependencyModule.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_GROUP", runtime.map { it.group.toString().enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_NAME", runtime.map { it.name.enquoted() })
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_VERSION", runtime.map { it.version.toString().enquoted() })
    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
}

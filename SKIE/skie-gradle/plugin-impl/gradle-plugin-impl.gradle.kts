import co.touchlab.skie.buildsetup.main.tasks.GenerateKotlinVersionEnumTask
import co.touchlab.skie.buildsetup.util.dependencyCoordinate
import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.version.KotlinVersionAttribute

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
    target.compilations.named("main").configure {
        GenerateKotlinVersionEnumTask.register(
            kotlinCompilation = this,
            packageName = "co.touchlab.skie.plugin",
            makeEnumPublic = true,
            activeVersion = null,
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

buildConfig {
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")

    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")

    buildConfigField("String", "SKIE_KOTLIN_VERSION_ATTRIBUTE_NAME", KotlinVersionAttribute.attributeName.enquoted())

    val kotlinPlugin = project.provider { project(projects.kotlinCompiler.kotlinCompilerLinkerPlugin.path) }
    buildConfigField("String", "SKIE_KOTLIN_PLUGIN_COORDINATE", kotlinPlugin.map { it.dependencyCoordinate.enquoted() })

    val runtime = project.provider { project(projects.runtime.runtimeKotlin.path) }
    buildConfigField("String", "SKIE_KOTLIN_RUNTIME_COORDINATE", runtime.map { it.dependencyCoordinate.enquoted() })
}

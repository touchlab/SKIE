import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.gradleApiVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator

plugins {
    id("skie.shim")
    id("skie.publishable")

    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin Shim Implementation"
    description = "Implementation of the SKIE Gradle Plugin Shim API, used by the main plugin module to interact with Kotlin Gradle Plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // All dependencies should be `compileOnly` and instead brought in by `gradle-plugin` to minimize the amount of runtime-loaded artifacts.
                compileOnly(projects.gradle.gradlePluginShimApi)
                compileOnly(projects.common.configuration.configurationDeclaration)

                compileOnly(libs.ci.info)
                compileOnly(libs.jgit)
                compileOnly(libs.mixpanel)

                compileOnly(projects.common.analytics)
                compileOnly(projects.common.util)
            }
        }
    }
}

buildConfig {
    generator(
        ExpectActualBuildConfigGenerator(
            isActualImplementation = false,
            internalVisibility = false,
        )
    )

    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "")
    buildConfigField("String", "KOTLIN_TOOLING_VERSION", "")
    buildConfigField("String", "GRADLE_API_VERSION", "")
    buildConfigField("String", "RUNTIME_DEPENDENCY_GROUP", "")
    buildConfigField("String", "RUNTIME_DEPENDENCY_NAME", "")
    buildConfigField("String", "RUNTIME_DEPENDENCY_VERSION", "")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "")
    buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "")
}

multiDimensionTarget.configureSourceSet { sourceSet ->
    if (!sourceSet.isTarget || compilation.isTest) { return@configureSourceSet }

    buildConfig {
        this.sourceSets.named(kotlinSourceSet.name).configure {
            generator(ExpectActualBuildConfigGenerator(isActualImplementation = true, internalVisibility = false))
            className.set("BuildConfig")

            val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject

            buildConfigField("String", "KOTLIN_PLUGIN_GROUP", kotlinPlugin.group.toString().enquoted())
            buildConfigField("String", "KOTLIN_PLUGIN_NAME", kotlinPlugin.name.enquoted())
            buildConfigField("String", "KOTLIN_PLUGIN_VERSION", kotlinPlugin.version.toString().enquoted())
            buildConfigField("String", "KOTLIN_TOOLING_VERSION", sourceSet.kotlinToolingVersion.value.enquoted())
            buildConfigField("String", "GRADLE_API_VERSION", sourceSet.gradleApiVersion.value.enquoted())

            val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject }
            buildConfigField("String", "RUNTIME_DEPENDENCY_GROUP", runtime.map { it.group.toString().enquoted() })
            buildConfigField("String", "RUNTIME_DEPENDENCY_NAME", runtime.map { it.name.enquoted() })
            buildConfigField("String", "RUNTIME_DEPENDENCY_VERSION", runtime.map { it.version.toString().enquoted() })

            val pluginId: String by properties
            buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")

            buildConfigField("String", "MIXPANEL_PROJECT_TOKEN", "\"a4c9352b6713103c0f8621757a35b8c9\"")
        }
    }
}
